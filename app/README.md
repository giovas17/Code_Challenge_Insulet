# Code challenge (1 hora)

English version: [README.en.md](README.en.md)

Tu trabajo es completar `NoticeCoordinator`. No necesitas conocer ninguna app interna.

Los tests empiezan fallando. Cuando todos pasen, terminaste.

---

## Qué problema resuelves

Una app muestra un **aviso** (un diálogo) cuando pasa algo, por ejemplo “activa las notificaciones”.

Ese aviso puede pedirse **varias veces seguidas**:

- la pantalla se recrea
- el usuario toca el botón dos veces
- otro módulo manda el mismo evento otra vez

Si no controlas eso, el aviso aparece duplicado o la app crashea.

`NoticeCoordinator` es el único lugar que decide:

- ¿lo muestro?
- ¿lo oculto?
- ¿lo ignoro?

---

## Piezas que ya existen (no las implementes)

| Clase | Qué es |
|--------|--------|
| `NoticeEvent` | Un evento que llega del resto de la app. Puede ser `null`. |
| `UiHost` | La pantalla. Tiene `isAttached`, `showNotice()` y `hideNotice()`. |
| `DialogStore` | Persistencia simple: `save`, `delete`, `exists`. |
| `InMemoryDialogStore` | Implementación de `DialogStore` en memoria. Ya funciona. |

### `NoticeEvent`

```kotlin
data class NoticeEvent(
    val shouldShow: Boolean,          // true = hay que mostrar el aviso
    val blockingFlowActive: Boolean,  // true = hay otro flujo abierto; no mostrar
)
```

### `UiHost`

- `isAttached == true` → la pantalla existe. Puedes llamar `showNotice()` / `hideNotice()`.
- `isAttached == false` → la pantalla se destruyó. **No** llames a esos métodos (crashea).

### Reloj (`nowMs`)

El constructor recibe `nowMs: () -> Long`. Úsalo para el debounce. **No** uses `Thread.sleep`. En los tests el tiempo se avanza a mano.

---

## Qué tienes que implementar

Archivo: `src/main/kotlin/challenge/NoticeCoordinator.kt`

Métodos:

| Método | Cuándo se llama |
|--------|-----------------|
| `onEvent(event)` | Llega un evento. `event` puede ser `null`. |
| `onHostAttached()` | La pantalla volvió a existir. |
| `onHostDetached()` | La pantalla se destruyó. |
| `onUserConfirmed()` | El usuario tocó Confirmar. |
| `onUserCancelled()` | El usuario tocó Cancelar. |
| `onAppRestarted()` | La app se reinició (proceso nuevo, **mismo** store). |
| `isNoticeVisible()` | `true` solo si el aviso está visible **ahora** en una pantalla attached. |

Constantes ya definidas (úsalas):

- `PENDING_ID` — “hay que mostrar el aviso”
- `DISMISSED_ID` — “el usuario ya lo cerró”
- `DEBOUNCE_MS = 500` — ignorar un segundo Confirmar/Cancelar si llega antes de 500 ms

---

## Comportamiento esperado (con ejemplos)

### 1. Evento null

`onEvent(null)` no hace nada y no crashea.

### 2. Mostrar la primera vez

`onEvent(NoticeEvent(shouldShow = true))` y la pantalla está attached:

- llama `ui.showNotice()` **una vez**
- `isNoticeVisible()` = true
- guarda `PENDING_ID` en el store

### 3. No duplicar

Si el aviso ya está visible y llega el mismo evento otra vez:

- **no** vuelvas a llamar `showNotice()`

### 4. Flujo bloqueante

`NoticeEvent(shouldShow = true, blockingFlowActive = true)`:

- no muestres
- no guardes `PENDING_ID`

### 5. Pantalla no attached

Si `ui.isAttached == false` y llega `shouldShow = true`:

- **no** llames `showNotice()` ni `hideNotice()`
- sí guarda `PENDING_ID`
- `isNoticeVisible()` = false

Cuando llamen `onHostAttached()` (y `isAttached` ya es true):

- si existe `PENDING_ID` y **no** existe `DISMISSED_ID` → entonces sí `showNotice()`

### 6. Confirmar o cancelar

`onUserConfirmed()` y `onUserCancelled()` hacen lo mismo:

- si el aviso estaba visible y la pantalla attached → `hideNotice()`
- borra `PENDING_ID`
- guarda `DISMISSED_ID`
- `isNoticeVisible()` = false

Si la pantalla **no** está attached: no llames `hideNotice()`, pero sí actualiza el store (pending off, dismissed on).

### 7. Debounce

Si Confirmar/Cancelar se llama otra vez en menos de **500 ms** (usa `nowMs()`):

- ignóralo
- no llames `hideNotice()` otra vez

Después de 500 ms, un nuevo ciclo sí puede confirmar otra vez.

### 8. Reinicio de la app

Los tests crean un **nuevo** `NoticeCoordinator` con el **mismo** store.

- Si el usuario ya confirmó/canceló → `onAppRestarted()` **no** muestra el aviso.
- Si quedó `PENDING_ID` y no hay `DISMISSED_ID` y el host está attached → **sí** lo muestra.

### 9. Ya no aplica

`onEvent(NoticeEvent(shouldShow = false))`:

- oculta el aviso si estaba visible y attached
- borra `PENDING_ID` **y** `DISMISSED_ID`

Así, si más tarde `shouldShow` vuelve a `true`, se puede mostrar de nuevo.

---

## Cómo correr los tests

Desde esta carpeta (no uses el Gradle de la app Android):

```bash
cd code-challenge
gradle test
```

Los tests están en `src/test/kotlin/challenge/NoticeCoordinatorTest.kt`. Léelos: son ejemplos concretos de cada regla.

---

## Tiempo sugerido

1. Lee este README y abre los tests (15 min)
2. Implementa haciendo pasar un test a la vez (35 min)
3. Explica qué guarda evita cada problema (10 min)
