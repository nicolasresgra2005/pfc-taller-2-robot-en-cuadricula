package taller

import org.scalatest.funsuite.AnyFunSuite

class RobotCuadriculaCasosPropiosTest extends AnyFunSuite {
  val robot = new RobotCuadricula()
  import robot._

  test("girar cubre los cuatro giros a la derecha") {
    assert(List(Norte, Este, Sur, Oeste).map(d => girar(d, Derecha)) ==
      List(Este, Sur, Oeste, Norte))
  }

  test("girar cubre los cuatro giros a la izquierda") {
    assert(List(Norte, Este, Sur, Oeste).map(d => girar(d, Izquierda)) ==
      List(Oeste, Norte, Este, Sur))
  }

  test("girar dos veces a la derecha equivale a mirar al punto opuesto") {
    assert(List(Norte, Este, Sur, Oeste).map(d => girar(girar(d, Derecha), Derecha)) ==
      List(Sur, Oeste, Norte, Este))
  }

  test("girar tres veces a la izquierda equivale a una vez a la derecha") {
    val direcciones = List(Norte, Este, Sur, Oeste)
    assert(direcciones.forall(d =>
      List.fill(3)(Izquierda).foldLeft(d: Direccion)((actual, s) => girar(actual, s)) ==
        girar(d, Derecha)))
  }

  test("girar un giro y su opuesto conservan cualquier dirección") {
    val direcciones = List(Norte, Este, Sur, Oeste)
    assert(direcciones.forall(d =>
      girar(girar(d, Derecha), Izquierda) == d &&
        girar(girar(d, Izquierda), Derecha) == d))
  }

  test("ejecutar avanza desde posiciones arbitrarias en las cuatro direcciones") {
    assert(List(
      ejecutar(Estado(2, 3, Norte), Avanzar(4)),
      ejecutar(Estado(2, 3, Este), Avanzar(4)),
      ejecutar(Estado(2, 3, Sur), Avanzar(4)),
      ejecutar(Estado(2, 3, Oeste), Avanzar(4))) ==
      List(Estado(2, 7, Norte), Estado(6, 3, Este),
        Estado(2, -1, Sur), Estado(-2, 3, Oeste)))
  }

  test("ejecutar procesa giros y avances negativos en una secuencia") {
    val orden = Secuencia(List(Girar(Izquierda), Avanzar(-3), Girar(Derecha)))
    assert(ejecutar(Estado(4, 2, Norte), orden) == Estado(7, 2, Norte))
  }

  test("ejecutar una repetición negativa conserva el estado inicial") {
    val estado = Estado(-3, 8, Oeste)
    assert(ejecutar(estado, Repetir(-2, Avanzar(11))) == estado)
  }

  test("ejecutar repeticiones anidadas en el orden especificado") {
    val orden = Repetir(2, Secuencia(List(Avanzar(2), Repetir(2, Girar(Derecha)))))
    assert(ejecutar(inicial, orden) == Estado(0, 0, Norte))
  }

  test("ejecutar preserva las coordenadas al girar") {
    assert(ejecutar(Estado(-7, 12, Este), Girar(Izquierda)) == Estado(-7, 12, Norte))
  }

  test("ejecutar una secuencia de órdenes de giro y avance sucesivas") {
    val orden = Secuencia(List(Girar(Derecha), Avanzar(3), Girar(Derecha), Avanzar(5)))
    assert(ejecutar(Estado(1, 1, Norte), orden) == Estado(4, -4, Sur))
  }

  test("cuantasBasicas suma alternativas y multiplica repeticiones anidadas") {
    val orden = Secuencia(List(
      Avanzar(4),
      Repetir(3, Secuencia(List(Girar(Derecha), Repetir(2, Avanzar(-1)))))))
    assert(cuantasBasicas(orden) == 10)
  }

  test("cuantasBasicas cuenta giros como básicas aunque no muevan el robot") {
    assert(cuantasBasicas(Secuencia(List(Girar(Izquierda), Girar(Derecha), Avanzar(0)))) == 3)
  }

  test("cuantasBasicas trata una repetición negativa como cero ejecuciones") {
    assert(cuantasBasicas(Repetir(-5, Secuencia(List(Avanzar(1), Girar(Derecha))))) == 0)
  }

  test("profundidad distingue secuencias anidadas de sus órdenes básicas") {
    assert(profundidad(Secuencia(List(Avanzar(1), Secuencia(List(Girar(Derecha)))))) == 3)
  }

  test("profundidad suma niveles de repetición aunque el contador sea cero") {
    assert(profundidad(Repetir(0, Repetir(2, Avanzar(1)))) == 3)
  }

  test("componer respeta el orden observable de las órdenes") {
    val composicion = componer(Girar(Derecha), Avanzar(6))
    assert(ejecutar(Estado(2, 2, Norte), composicion) == Estado(8, 2, Este))
  }

  test("rutina construye repeticiones con pasos negativos") {
    assert(rutina(-2)(Izquierda)(3) ==
      Repetir(3, Secuencia(List(Avanzar(-2), Girar(Izquierda)))))
  }

  test("aplicarA mantiene el orden de estados y devuelve un resultado por estado") {
    val estados = List(Estado(1, 4, Norte), Estado(8, -2, Oeste), Estado(0, 0, Sur))
    assert(aplicarA(estados, Avanzar(3)) ==
      List(Estado(1, 7, Norte), Estado(5, -2, Oeste), Estado(0, -3, Sur)))
  }

  test("transformar aplica la función a todas las básicas y conserva los contenedores") {
    val orden = Repetir(2, Secuencia(List(Avanzar(3), Girar(Derecha))))
    val rotar = transformar(orden, {
      case Girar(Derecha) => Girar(Izquierda)
      case basica => basica
    })
    assert(rotar == Repetir(2, Secuencia(List(Avanzar(3), Girar(Izquierda)))))
  }

  test("transformar aplica también la función a una orden básica en la raíz") {
    assert(transformar(Girar(Izquierda), _ => Avanzar(9)) == Avanzar(9))
  }

  test("simplificar reduce órdenes nulas y singleton en niveles anidados") {
    val orden = Repetir(4, Secuencia(List(Secuencia(List(Avanzar(0))), Avanzar(7))))
    assert(simplificar(orden) == Repetir(4, Avanzar(7)))
  }

  test("simplificar vuelve no-op una repetición positiva de una orden vacía") {
    assert(simplificar(Repetir(8, Secuencia(Nil))) == Secuencia(Nil))
  }

  test("simplificar combina avances consecutivos incluso si una suma intermedia es cero") {
    val orden = Secuencia(List(Avanzar(5), Avanzar(2), Avanzar(-2), Girar(Derecha)))
    assert(simplificar(orden) == Secuencia(List(Avanzar(5), Girar(Derecha))))
  }

  test("simplificar es idempotente en composiciones anidadas") {
    val orden = Repetir(3, Secuencia(List(
      Repetir(1, Secuencia(List(Avanzar(2), Avanzar(-2)))),
      Secuencia(List(Girar(Izquierda))))))
    assert(simplificar(simplificar(orden)) == simplificar(orden))
  }

  test("simplificar conserva la ejecución para estados y órdenes anidados") {
    val ordenes = List(
      Secuencia(List(Avanzar(3), Avanzar(-1), Girar(Izquierda), Avanzar(2))),
      Repetir(3, Secuencia(List(Repetir(1, Avanzar(4)), Girar(Derecha)))),
      Secuencia(List(Repetir(0, Avanzar(6)), Avanzar(0), Girar(Izquierda))))
    val estados = List(inicial, Estado(-4, 9, Este), Estado(12, -3, Sur))
    assert(ordenes.forall(orden =>
      estados.forall(estado =>
        ejecutar(estado, orden) == ejecutar(estado, simplificar(orden)))))
  }
}
