# Manual de usuario - Detectar la ciudad automáticamente

## Historia de usuario 532319

La aplicación detecta la ciudad en la que se encuentra el usuario a partir de la ubicación del móvil y abre directamente la lista de estaciones de esa ciudad, sin necesidad de buscarla en la lista de redes.

## Requisitos

- Haber dado permiso de ubicación a la aplicación (con la ubicación aproximada es suficiente).
- Tener la ubicación del móvil activada.
- Estar a 20 km o menos de una ciudad con red de bicicletas.

## Procedimiento

1. Abrir la aplicación Bicis.
2. La aplicación carga las redes de bicicletas disponibles.
3. Cuando las redes se han cargado, la aplicación pide permiso para usar la ubicación. Pulsar "Permitir".
4. La aplicación obtiene la ubicación y busca la red más cercana, hasta un máximo de 20 km.
5. Se abre directamente la lista de estaciones de esa red y se muestra el nombre de la ciudad detectada (por ejemplo, Santander).

## Resultado esperado

Se muestran las estaciones de la red de la ciudad en la que se encuentra el usuario. Si hay varias redes dentro de los 20 km, se muestra la más cercana.

Para saber la ciudad no se usa ningún servicio externo: la aplicación calcula la distancia en línea recta desde el usuario hasta el centro de cada red.

## Gestión de errores

En todos los casos de aviso, la aplicación muestra la lista con todas las ciudades para que el usuario elija una a mano.

| Situación | Mensaje |
|---|---|
| El usuario no da permiso de ubicación | "No has dado permiso para usar tu ubicación. Elige una ciudad de la lista" |
| No se puede obtener la ubicación (ubicación desactivada, sin señal o tarda demasiado) | "No se ha podido obtener tu ubicación. Elige una ciudad de la lista" |
| No hay ninguna red a 20 km o menos | "No hay ninguna red de bicis en tu ciudad. Elige una ciudad de la lista" |

Si no se pueden cargar las redes (sin conexión o el servicio no responde), la aplicación muestra "Ha habido un error cargando las redes", no pide el permiso de ubicación ni intenta detectar la ciudad, y no se cierra.

## Observaciones

El paso 5 está pendiente de validar cuando se integre la apertura directa de la lista de estaciones.