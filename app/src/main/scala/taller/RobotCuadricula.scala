package taller

import scala.annotation.tailrec

/**
 * Taller 2 — órdenes para un robot en una cuadrícula infinita.
 *
 * El tipo `Orden` es recursivo: `Repetir` y `Secuencia` contienen otras
 * órdenes. Toda función que lo recorra lo es también, con un caso por cada
 * alternativa del tipo.
 */
class RobotCuadricula {

  /** El robot empieza en el origen mirando al norte. */
  val inicial = Estado(0, 0, Norte)

  // Punto 1 -------------------------------------------------------------------

  /** La dirección que queda tras girar noventa grados hacia el sentido dado. */
  def girar(d: Direccion, s: Sentido): Direccion = (d, s) match {
    case (Norte, Derecha) => Este
    case (Este, Derecha) => Sur
    case (Sur, Derecha) => Oeste
    case (Oeste, Derecha) => Norte
    case (Norte, Izquierda) => Oeste
    case (Oeste, Izquierda) => Sur
    case (Sur, Izquierda) => Este
    case (Este, Izquierda) => Norte
  }

  // Punto 2 -------------------------------------------------------------------

  /** El estado que resulta de aplicarle la orden al estado dado. */
  def ejecutar(estado: Estado, orden: Orden): Estado = orden match {
    case Avanzar(n) =>
      estado.mira match {
        case Norte => estado.copy(y = estado.y + n)
        case Este => estado.copy(x = estado.x + n)
        case Sur => estado.copy(y = estado.y - n)
        case Oeste => estado.copy(x = estado.x - n)
      }
    case Girar(sentido) => estado.copy(mira = girar(estado.mira, sentido))
    case Repetir(veces, ordenInterna) =>
      @tailrec
      def repetir(restantes: Int, estadoActual: Estado): Estado =
        if (restantes <= 0) estadoActual
        else repetir(restantes - 1, ejecutar(estadoActual, ordenInterna))

      repetir(veces, estado)
    case Secuencia(ordenes) =>
      ordenes.foldLeft(estado) { (estadoActual, siguienteOrden) =>
        ejecutar(estadoActual, siguienteOrden)
      }
  }

  // Punto 3 -------------------------------------------------------------------

  /** Cuántos Avanzar y Girar llega a ejecutar la orden, contando repeticiones. */
  def cuantasBasicas(orden: Orden): Int = orden match {
    case Avanzar(_) => 1
    case Girar(_) => 1
    case Repetir(veces, ordenInterna) =>
      if (veces <= 0) 0 else veces * cuantasBasicas(ordenInterna)
    case Secuencia(ordenes) => ordenes.map(cuantasBasicas).sum
  }

  /** Niveles de anidamiento: una básica tiene 1, una compuesta uno más que la más honda. */
  def profundidad(orden: Orden): Int = orden match {
    case Avanzar(_) => 1
    case Girar(_) => 1
    case Repetir(_, ordenInterna) => 1 + profundidad(ordenInterna)
    case Secuencia(ordenes) =>
      val mayorProfundidad = ordenes.foldLeft(0) { (maxima, siguienteOrden) =>
        math.max(maxima, profundidad(siguienteOrden))
      }
      1 + mayorProfundidad
  }

  // Punto 4 -------------------------------------------------------------------

  /** La secuencia de las dos órdenes. */
  def componer(o1: Orden, o2: Orden): Orden = Secuencia(List(o1, o2))

  /** Currificada: fijados los pasos y el giro, queda una función de las veces. */
  def rutina(pasos: Int)(giro: Sentido)(veces: Int): Orden =
    Repetir(veces, Secuencia(List(Avanzar(pasos), Girar(giro))))

  /** La misma orden ejecutada sobre cada uno de los estados. */
  def aplicarA(estados: List[Estado], o: Orden): List[Estado] =
    estados.map(estado => ejecutar(estado, o))

  /** Reemplaza cada orden básica por f(orden), respetando la estructura de las compuestas. */
  def transformar(orden: Orden, f: Orden => Orden): Orden = orden match {
    case basica @ Avanzar(_) => f(basica)
    case basica @ Girar(_) => f(basica)
    case Repetir(veces, ordenInterna) => Repetir(veces, transformar(ordenInterna, f))
    case Secuencia(ordenes) =>
      Secuencia(ordenes.map(siguienteOrden => transformar(siguienteOrden, f)))
  }

  // Punto 5 -------------------------------------------------------------------

  /** Una orden equivalente más corta, con las reglas del enunciado. */
  def simplificar(o: Orden): Orden = {
    def noHaceNada(orden: Orden): Boolean = orden match {
      case Avanzar(0) => true
      case Repetir(veces, _) if veces <= 0 => true
      case Secuencia(Nil) => true
      case _ => false
    }

    def simplificarSecuencia(ordenes: List[Orden]): List[Orden] = {
      def combinar(restantes: List[Orden], acumuladas: List[Orden]): List[Orden] =
        restantes match {
          case Nil => acumuladas.reverse
          case Avanzar(pasos) :: cola =>
            acumuladas match {
              case Avanzar(anteriores) :: previas =>
                val total = anteriores + pasos
                val nuevas = if (total == 0) previas else Avanzar(total) :: previas
                combinar(cola, nuevas)
              case _ => combinar(cola, Avanzar(pasos) :: acumuladas)
            }
          case orden :: cola =>
            if (noHaceNada(orden)) combinar(cola, acumuladas)
            else combinar(cola, orden :: acumuladas)
        }

      combinar(ordenes.map(simplificar), Nil)
    }

    o match {
      case Avanzar(0) => Secuencia(Nil)
      case orden @ Avanzar(_) => orden
      case orden @ Girar(_) => orden
      case Repetir(veces, _) if veces <= 0 => Secuencia(Nil)
      case Repetir(1, orden) => simplificar(orden)
      case Repetir(veces, orden) =>
        val simplificada = simplificar(orden)
        if (noHaceNada(simplificada)) Secuencia(Nil)
        else Repetir(veces, simplificada)
      case Secuencia(ordenes) =>
        simplificarSecuencia(ordenes) match {
          case Nil => Secuencia(Nil)
          case orden :: Nil => orden
          case simplificadas => Secuencia(simplificadas)
        }
    }
  }
}
