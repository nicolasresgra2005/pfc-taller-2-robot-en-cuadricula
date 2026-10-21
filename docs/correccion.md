# Informe de corrección

Para hablar del resultado de una orden, usaré la expresión
$E(e,o)=\texttt{ejecutar(e,o)}$, donde $e$ es un estado y $o$ una orden.
La idea es revisar que cada caso del programa coincida con lo que define el
taller.

## Giro y ejecución

En `girar` hay un caso para cada combinación de dirección y sentido. Los
giros a la derecha siguen el ciclo Norte, Este, Sur, Oeste; los giros a la
izquierda siguen el orden contrario. Por lo tanto, cada combinación devuelve
la dirección que corresponde a un giro de noventa grados.

Para `ejecutar`, reviso las cuatro alternativas de `Orden`:

- Si $o=\operatorname{Avanzar}(n)$, el programa actualiza la coordenada que
  corresponde a la dirección del robot y deja la otra coordenada y la
  dirección iguales. La resta para Sur y Oeste es necesaria porque esos
  movimientos disminuyen `y` y `x`, respectivamente. Si $n$ es negativo, la
  misma operación produce un retroceso.
- Si $o=\operatorname{Girar}(s)$, el programa cambia la dirección con
  `girar` y mantiene las coordenadas mediante `copy`. Esto cumple que girar
  no desplaza al robot.
- Si $o=\operatorname{Repetir}(k,a)$ y $k\leq0$, se devuelve el estado
  original. Si $k>0$, la función interna ejecuta $a$ y vuelve a llamarse con
  una repetición menos. Por inducción sobre $k$, después de la primera
  ejecución quedan $k-1$ ejecuciones; cuando el contador llega a cero, ya se
  aplicó $a$ exactamente $k$ veces.
- Si $o=\operatorname{Secuencia}(as)$, `foldLeft` empieza con el estado $e$
  y usa como estado siguiente el resultado de ejecutar cada orden. De esta
  forma mantiene el orden de la lista. Si la lista está vacía, no se aplica
  ninguna orden y se conserva $e$.

## Conteo y profundidad

La corrección de `cuantasBasicas` se obtiene por inducción estructural sobre
la orden. En los casos básicos (`Avanzar` y `Girar`) el resultado es uno. En
una secuencia, el número total es la suma de los conteos de sus elementos;
en una repetición positiva, el número interior se cuenta tantas veces como
indica el contador. Para un contador no positivo el total es cero. Estos
casos corresponden exactamente a cuántas órdenes básicas se llegan a
ejecutar.

Para `profundidad`, los casos básicos tienen profundidad uno. Una repetición
tiene un nivel adicional respecto a la orden que contiene. Una secuencia
tiene un nivel adicional respecto al elemento más profundo. Si la secuencia
está vacía, el máximo interior se toma como cero, por lo que su profundidad
es uno. Así, la definición del programa coincide con la definición de
profundidad del enunciado.

## Composición y funciones que reciben funciones

`componer(a,b)` construye `Secuencia(List(a,b))`. Por cómo funciona la
secuencia, se cumple que:

$$
E(e,\operatorname{componer}(a,b)) = E(E(e,a),b).
$$

Es decir, el estado después de ejecutar la composición es el mismo que se
obtiene ejecutando primero $a$ y luego $b$.

`rutina` construye una repetición de la secuencia formada por un avance y un
giro. Si llamamos $b$ a
$\operatorname{Secuencia}([\operatorname{Avanzar}(p),
\operatorname{Girar}(s)])$, entonces `rutina(p)(s)(k)` construye
$\operatorname{Repetir}(k,b)$. Para $k\leq0$, la rutina no cambia el estado.
Para $k>0$, por la regla ya demostrada de `Repetir`, ejecuta $b$ exactamente
$k$ veces. Cada ejecución de $b$ equivale a avanzar $p$ y después girar $s$,
por la regla de secuencia. Esto justifica que la rutina compuesta equivale a
repetir sus dos pasos en ese orden.

`aplicarA` usa `map`: si la lista de estados es $[e_1,\ldots,e_n]`, devuelve
$[E(e_1,o),\ldots,E(e_n,o)]$. `map` conserva el orden y la cantidad de
elementos, así que cada estado recibe la misma orden una vez.

En `transformar`, los casos básicos se reemplazan por el resultado de aplicar
$f$. En los casos compuestos se mantiene la alternativa original y se llama
recursivamente para sus elementos. Por inducción estructural, transforma
todas las órdenes básicas que contiene y no cambia la organización de las
repeticiones ni de las secuencias.

## Por qué `simplificar` conserva el resultado

La propiedad que queremos mostrar es:

$$
\forall e,\ o:\quad E(e,o)=E(e,\operatorname{simplificar}(o)).
$$

La demostración se hace por inducción estructural y usando las reglas de
simplificación.

En los casos básicos, `Avanzar(0)` no cambia el estado, igual que
`Secuencia(Nil)`. Un avance distinto de cero y un giro se conservan, así que
su resultado tampoco cambia.

Para las órdenes compuestas, suponemos que simplificar las órdenes internas
no cambia lo que hacen:

1. **Repetición no positiva.** `Repetir(k,a)` con $k\leq0$ no ejecuta $a$ y
   deja el estado igual. Se reemplaza por `Secuencia(Nil)`, que también deja
   el estado igual.
2. **Repetición de una vez.** `Repetir(1,a)` ejecuta $a$ exactamente una
   vez. Por la hipótesis inductiva, sustituir $a$ por su versión simplificada
   mantiene el resultado.
3. **Quitar una orden que no hace nada.** `Avanzar(0)`, una repetición no
   positiva y una secuencia vacía dejan el estado como estaba. Quitar una de
   estas órdenes de una secuencia no afecta el estado que reciben las
   órdenes siguientes. Una repetición positiva de una orden sin efecto
   tampoco cambia el estado.
4. **Juntar dos avances seguidos.** Si el robot mira en una dirección $d$,
   avanzar $m$ y después $n$ desplaza la misma coordenada que avanzar
   $m+n$. La dirección no cambia durante esos avances. Si $m+n=0$, el
   desplazamiento total es cero y se puede quitar.
5. **Reducir una secuencia.** Una secuencia vacía es la identidad y una
   secuencia de un elemento hace lo mismo que ese elemento. Para las demás,
   se pueden eliminar identidades y reemplazar dos avances consecutivos por
   uno equivalente. Como cada reemplazo conserva el estado que pasa a la
   siguiente orden, el resultado de toda la secuencia se conserva.

La simplificación aplica estas reglas también a las órdenes internas. Como
cada paso preserva el estado y las órdenes compuestas ejecutan sus
componentes en orden, concluimos que ejecutar cualquier orden produce el
mismo estado que ejecutar su versión simplificada.
