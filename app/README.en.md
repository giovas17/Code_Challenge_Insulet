# Code challenge (1 hour)

Versión en español: [README.md](README.md)

Your job is to complete `NoticeCoordinator`. You do not need any internal product knowledge.

The tests start red. When they all pass, you are done.

---

## What problem you are solving

An app shows a **notice** (a dialog) when something happens, for example “turn notifications on”.

That notice can be requested **several times in a row**:

- the screen is recreated
- the user taps the button twice
- another module sends the same event again

If you do not control that, the notice appears twice or the app crashes.

`NoticeCoordinator` is the single place that decides:

- should I show it?
- should I hide it?
- should I ignore it?

---

## Pieces that already exist (do not implement them)

| Class | What it is |
|--------|--------|
| `NoticeEvent` | An event from the rest of the app. It can be `null`. |
| `UiHost` | The screen. It has `isAttached`, `showNotice()`, and `hideNotice()`. |
| `DialogStore` | Simple persistence: `save`, `delete`, `exists`. |
| `InMemoryDialogStore` | In-memory `DialogStore`. It already works. |

### `NoticeEvent`

```kotlin
data class NoticeEvent(
    val shouldShow: Boolean,          // true = the notice should be shown
    val blockingFlowActive: Boolean,  // true = another flow is open; do not show
)
```

### `UiHost`

- `isAttached == true` → the screen exists. You may call `showNotice()` / `hideNotice()`.
- `isAttached == false` → the screen was destroyed. **Do not** call those methods (it crashes).

### Clock (`nowMs`)

The constructor takes `nowMs: () -> Long`. Use it for debounce. **Do not** use `Thread.sleep`. Tests advance time manually.

---

## What you need to implement

File: `src/main/kotlin/challenge/NoticeCoordinator.kt`

Methods:

| Method | When it is called |
|--------|-------------------|
| `onEvent(event)` | An event arrived. `event` may be `null`. |
| `onHostAttached()` | The screen exists again. |
| `onHostDetached()` | The screen was destroyed. |
| `onUserConfirmed()` | The user tapped Confirm. |
| `onUserCancelled()` | The user tapped Cancel. |
| `onAppRestarted()` | The app restarted (new process, **same** store). |
| `isNoticeVisible()` | `true` only if the notice is visible **right now** on an attached screen. |

Constants already defined (use them):

- `PENDING_ID` — “the notice still needs to be shown”
- `DISMISSED_ID` — “the user already closed it”
- `DEBOUNCE_MS = 500` — ignore a second Confirm/Cancel if it arrives within 500 ms

---

## Expected behavior (with examples)

### 1. Null event

`onEvent(null)` does nothing and does not crash.

### 2. Show the first time

`onEvent(NoticeEvent(shouldShow = true))` and the screen is attached:

- call `ui.showNotice()` **once**
- `isNoticeVisible()` = true
- save `PENDING_ID` in the store

### 3. Do not duplicate

If the notice is already visible and the same event arrives again:

- **do not** call `showNotice()` again

### 4. Blocking flow

`NoticeEvent(shouldShow = true, blockingFlowActive = true)`:

- do not show
- do not save `PENDING_ID`

### 5. Screen not attached

If `ui.isAttached == false` and `shouldShow = true` arrives:

- **do not** call `showNotice()` or `hideNotice()`
- do save `PENDING_ID`
- `isNoticeVisible()` = false

When `onHostAttached()` is called (and `isAttached` is already true):

- if `PENDING_ID` exists and `DISMISSED_ID` does **not** → then call `showNotice()`

### 6. Confirm or cancel

`onUserConfirmed()` and `onUserCancelled()` do the same thing:

- if the notice was visible and the screen is attached → `hideNotice()`
- delete `PENDING_ID`
- save `DISMISSED_ID`
- `isNoticeVisible()` = false

If the screen is **not** attached: do not call `hideNotice()`, but still update the store (pending off, dismissed on).

### 7. Debounce

If Confirm/Cancel is called again in less than **500 ms** (use `nowMs()`):

- ignore it
- do not call `hideNotice()` again

After 500 ms, a new cycle may confirm again.

### 8. App restart

Tests create a **new** `NoticeCoordinator` with the **same** store.

- If the user already confirmed/cancelled → `onAppRestarted()` does **not** show the notice.
- If `PENDING_ID` is set, `DISMISSED_ID` is not, and the host is attached → **do** show it.

### 9. Condition no longer applies

`onEvent(NoticeEvent(shouldShow = false))`:

- hide the notice if it was visible and attached
- delete `PENDING_ID` **and** `DISMISSED_ID`

That way, if `shouldShow` becomes `true` later, it can be shown again.

---

## How to run the tests

From this folder (do not use the Android app Gradle):

```bash
cd code-challenge
gradle test
```

Tests live in `src/test/kotlin/challenge/NoticeCoordinatorTest.kt`. Read them: they are concrete examples of each rule.

---

## Suggested time

1. Read this README and open the tests (15 min)
2. Implement by making tests pass one at a time (35 min)
3. Explain which guard prevents each problem (10 min)
