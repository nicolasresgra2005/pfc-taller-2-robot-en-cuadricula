# Taller 2 — Un robot en la cuadrícula

Fundamentos de Programación Funcional y Concurrente
Escuela de Ingeniería de Sistemas y Computación, Universidad del Valle
Carlos Andrés Delgado Saavedra

El enunciado completo, con los ejemplos de cada punto, la rúbrica y la
ecuación de calificación, es el PDF publicado en el campus virtual. Este
archivo dice cómo está armado el proyecto y cómo se entrega.

## Integrantes

La tabla debe incluir el nombre completo y el código de cada integrante.
Es parte de la entrega: si falta alguien, se aplica la sanción indicada en el
enunciado.

| Nombre completo | Código |
|---|---|
| Nicolas Restrepo Grajales | 2380471-2724 |

## Cómo está organizado el proyecto

```
app/src/main/scala/taller/
    Modelo.scala              los tipos del taller, que no se modifican
    RobotCuadricula.scala     aquí van los cinco puntos
    App.scala                 programa de arranque

app/src/test/scala/taller/
    RobotCuadriculaTest.scala   las 40 pruebas dadas, que no se modifican
    RobotCuadriculaCasosPropiosTest.scala
                               pruebas propias: cinco por cada punto

docs/                         los informes, en Markdown
```

La solución de los cinco puntos está en `RobotCuadricula.scala`. Las pruebas
originales están en `RobotCuadriculaTest.scala` y se mantienen aparte de las
25 pruebas propias de `RobotCuadriculaCasosPropiosTest.scala`.

Los informes de proceso y de corrección que pide el enunciado van en `docs/`,
en Markdown, con la notación matemática en LaTeX y los diagramas en
`mermaid`; no se aceptan imágenes insertadas ni archivos por fuera de esa
carpeta.

## Cómo se ejecuta

```bash
./gradlew test    # revisa las reglas del curso y corre las pruebas
./gradlew run     # corre el programa
```

La primera vez se demora: Gradle descarga el compilador de Scala y la
versión de Java que necesita. No hay que instalar nada a mano. Al terminar,
`./gradlew test` deja un informe navegable en
`app/build/reports/tests/test/index.html`.

## Modelo y solución

`Modelo.scala` define los tipos del taller: `Direccion` con sus cuatro puntos
cardinales, `Sentido`, `Orden` con sus cuatro alternativas y `Estado`. No se
modifica. `RobotCuadricula.scala` implementa los cinco puntos sobre ese
modelo.

El tipo `Orden` es recursivo: `Repetir` y `Secuencia` contienen otras
órdenes. Toda función que lo recorra lo es también, y se escribe con `match`
sobre las alternativas del tipo, un caso por cada una.

## Los cinco puntos

| Punto | Función |
|---|---|
| 1 | `girar(d: Direccion, s: Sentido): Direccion` |
| 2 | `ejecutar(e: Estado, o: Orden): Estado` |
| 3 | `cuantasBasicas(o: Orden): Int` y `profundidad(o: Orden): Int` |
| 4 | `componer`, `rutina(pasos)(giro)(veces)`, `aplicarA` y `transformar(o, f)` |
| 5 | `simplificar(o: Orden): Orden` |

El punto 1 se resuelve con `match` sobre la pareja `(d, s)`, sin
condicionales encadenados. El 4 tiene la función currificada y las dos que
reciben o devuelven funciones. El 5 es el más largo: las reglas se aplican
tantas veces como haga falta, porque una pasada puede habilitar la
siguiente.

## Reglas del código

En este curso no se usa `var`, ni `while`, ni `return`, ni ningún estado que
cambie. Todo se resuelve con `val`, recursión y llamados a funciones. Si
necesita una secuencia de instrucciones, use un bloque `{ ... }`; si necesita
funciones auxiliares, escríbalas dentro de las funciones.

`./gradlew test` revisa estas reglas **antes** de correr las pruebas. Un
programa con `var`, `while` o `return` no llega a las pruebas: el flujo se
detiene y dice en qué línea está el problema.

No se editan los archivos de configuración del proyecto ni los flujos de
GitHub.

## Cómo se entrega

1. Presione **Fork** en este repositorio. Un solo integrante hace el fork y
   agrega a los demás como colaboradores; todo el equipo trabaja sobre ese
   mismo fork.
2. En su fork, entre a la pestaña **Actions** y habilítelas. GitHub las deja
   apagadas en los forks hasta que el dueño lo autoriza, y sin eso no ve el
   resultado de las pruebas al hacer push.
3. Clone su fork, resuelva, y haga commit y push a `main`. Cada integrante
   hace sus propios commits: la rúbrica de aporte al código se calcula con
   ellos.
4. Registre en la tarea **Entrega del taller 2** del campus virtual la
   dirección de su fork y el hash del último commit, que se obtiene con
   `git rev-parse HEAD`.

**La fecha de entrega es el jueves 22 de octubre de 2026, a las 23:59.**

Las condiciones son estas, y son las del acta de inicio del curso:

- **La entrega es el enlace registrado en el campus dentro del plazo.** No se
  aceptan entregas por correo ni por ningún otro medio. Si el enlace no llega
  a tiempo, la entrega se califica con **0.0**.
- **El repositorio tiene que ser un fork de este.** Un repositorio creado
  aparte, aunque tenga el mismo código, se sanciona con el **30 %** de la
  nota: rompe la estructura con que se califica y la verificación automática.
- **Se califica el último commit anterior a la fecha de entrega.** Lo que se
  suba después no se revisa. El hash que se registra en el campus debe ser el
  de ese commit.
- **El `README.md` lleva el nombre y el código de todos los integrantes.**
  Si falta alguno, la entrega se sanciona con el **20 %** de la nota.
- Se puede trabajar en grupos de hasta cuatro personas.
- El docente puede pedir sustentación del taller. En ese caso la nota de la
  sustentación es individual, entre 0 y 1, y multiplica la nota del taller.
