# Informe de proceso

En este taller modelé un robot que se mueve en una cuadrícula. Su estado
tiene dos coordenadas y la dirección hacia la que mira. Las órdenes pueden
ser simples (`Avanzar` y `Girar`) o contener otras órdenes (`Repetir` y
`Secuencia`), por eso varias funciones usan recursión.

## 1. Cambiar la dirección

En `girar` reviso la dirección y el sentido al mismo tiempo con un `match`
sobre la pareja `(d, s)`. Si el sentido es derecha, sigo Norte, Este, Sur,
Oeste y vuelvo a Norte. Para izquierda recorro el mismo ciclo al revés.

Por ejemplo, si el robot mira al Oeste y gira a la derecha, queda mirando al
Norte. Si mira al Sur y gira a la izquierda, queda mirando al Este. En ambos
casos la posición se mantiene.

Con un estado concreto, la primera operación queda así:

| Operación | Estado |
|---|---|
| Inicio | `(3,-2,Oeste)` |
| Girar a la derecha | `(3,-2,Norte)` |
| Girar a la izquierda | `(3,-2,Oeste)` |

La segunda operación deshace la primera.

## 2. Ejecutar órdenes

En `ejecutar` hay un caso para cada tipo de orden:

- Para `Avanzar(n)`, cambio una sola coordenada. Si mira al Norte sumo `n` a
  `y`; si mira al Este sumo `n` a `x`. Al mirar al Sur u Oeste resto `n`.
  Así, un `n` negativo hace que el robot retroceda.
- Para `Girar(sentido)`, uso `girar` y copio el estado con la nueva
  dirección. Las coordenadas no cambian.
- Para `Repetir`, una función interna aplica la orden una vez por llamada y
  se detiene cuando ya no quedan repeticiones positivas.
- Para `Secuencia`, `foldLeft` pasa el estado que dejó una orden a la
  siguiente. Con una lista vacía, devuelve el estado con el que empezó.

La siguiente traza muestra una secuencia repetida cuatro veces. El robot
empieza en `(0,0)` mirando al Norte:

| Vuelta | Estado inicial | Después de avanzar | Después del giro |
|---|---|---|---|
| 1 | `(0,0,Norte)` | `(0,1,Norte)` | `(0,1,Este)` |
| 2 | `(0,1,Este)` | `(1,1,Este)` | `(1,1,Sur)` |
| 3 | `(1,1,Sur)` | `(1,0,Sur)` | `(1,0,Oeste)` |
| 4 | `(1,0,Oeste)` | `(0,0,Oeste)` | `(0,0,Norte)` |

La orden es
`Repetir(4, Secuencia(List(Avanzar(1), Girar(Derecha))))`. Como se ve en la
tabla, termina en el origen y mirando al Norte.

## 3. Contar órdenes y medir profundidad

`cuantasBasicas` cuenta los `Avanzar` y `Girar` que se ejecutarían. En una
secuencia sumo lo que aporta cada elemento; en una repetición multiplico el
conteo de la orden interior por las veces que se repite. Si las veces son
cero o negativas, no se ejecuta ninguna orden interior.

Por ejemplo,
`Repetir(3, Secuencia(List(Avanzar(1), Girar(Derecha))))` contiene seis
órdenes básicas: dos en cada vuelta.

`profundidad` mide cuántos niveles hay desde la orden hasta la orden más
interna. Una orden básica vale uno. Para `Repetir` agrego un nivel a la
profundidad interior; para `Secuencia`, tomo la profundidad máxima de sus
elementos y agrego uno. En una secuencia vacía no hay elementos, así que el
máximo se toma como cero y la profundidad queda en uno.

Para ver ambas medidas junto con el recorrido, tomo
`Repetir(2, Secuencia(List(Avanzar(1), Girar(Derecha))))`. Tiene cuatro
órdenes básicas y profundidad tres. Al ejecutarla desde el origen:

| Acción | Posición y dirección |
|---|---|
| Inicio | `(0,0,Norte)` |
| Primera vuelta: avanzar | `(0,1,Norte)` |
| Primera vuelta: girar | `(0,1,Este)` |
| Segunda vuelta: avanzar | `(1,1,Este)` |
| Segunda vuelta: girar | `(1,1,Sur)` |

## 4. Combinar y reutilizar órdenes

`componer(a,b)` arma una secuencia con `a` primero y `b` después. Por
ejemplo, empezando en `(2,2,Norte)`, componer `Avanzar(3)` con
`Girar(Derecha)` deja al robot en `(2,5,Este)`: primero sube tres casillas y
luego cambia de dirección.

`rutina(pasos)(giro)(veces)` devuelve una orden que repite un avance seguido
de un giro. Por ejemplo, `rutina(2)(Derecha)(2)` ejecuta dos veces
`Avanzar(2)` y luego `Girar(Derecha)`. Desde el estado inicial, su traza es:

| Acción | Estado |
|---|---|
| Inicio | `(0,0,Norte)` |
| Avanzar 2 | `(0,2,Norte)` |
| Girar a la derecha | `(0,2,Este)` |
| Avanzar 2 | `(2,2,Este)` |
| Girar a la derecha | `(2,2,Sur)` |

`aplicarA` hace lo mismo con varios estados: ejecuta la orden para cada uno y
devuelve los resultados en el mismo orden. Por ejemplo, al aplicar
`Avanzar(2)` a `(0,0,Norte)` y `(5,5,Sur)`, los resultados son
`(0,2,Norte)` y `(5,3,Sur)`.

`transformar` recorre una orden y llama a la función recibida para cada caso
básico. Si encuentra una repetición o una secuencia, vuelve a recorrer lo que
contienen y conserva la estructura. Por eso se puede, por ejemplo, duplicar
los pasos de cada avance sin cambiar los giros.

Si se duplican los avances de la rutina del ejemplo anterior, cada
`Avanzar(2)` se convierte en `Avanzar(4)`. El recorrido queda así:
`(0,0,Norte)`, `(0,4,Norte)`, `(0,4,Este)`, `(4,4,Este)` y `(4,4,Sur)`.
La dirección gira igual que antes, pero cada avance cubre el doble de
casillas.

## 5. Simplificar

En `simplificar` primero trabajo sobre las órdenes internas. Después aplico
las reglas que reducen la estructura: los avances de cero y las repeticiones
no positivas se convierten en `Secuencia(Nil)`; una repetición de una vez se
reemplaza por su orden; y las secuencias eliminan órdenes sin efecto. También
junto avances que están uno al lado del otro. Si su suma es cero, ese avance
se elimina. Al final, una secuencia con un único elemento se reduce a ese
elemento.

Este ejemplo muestra cómo se simplifican varias partes de la orden:

```text
Secuencia(List(Avanzar(0), Repetir(1, Secuencia(List(Avanzar(2), Avanzar(3)))),
              Avanzar(-5), Girar(Derecha)))
```

Al recorrer la orden, primero se juntan los avances interiores `2` y `3`, y
se elimina la repetición de una vez. En la secuencia exterior, `Avanzar(0)`
no aporta movimiento; además, `Avanzar(5)` y `Avanzar(-5)` se cancelan. Por
eso, el resultado final de simplificar toda la orden es
`Girar(Derecha)`.

También comparé la ejecución de las dos órdenes. Desde `(0,0,Norte)`, la
orden original queda en `(0,5,Norte)` después de los avances `2` y `3`,
vuelve a `(0,0,Norte)` con `Avanzar(-5)` y termina en `(0,0,Este)` al
girar. La orden simplificada `Girar(Derecha)` termina en el mismo estado.
