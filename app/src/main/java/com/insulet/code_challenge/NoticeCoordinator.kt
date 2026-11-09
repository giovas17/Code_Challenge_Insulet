package com.insulet.code_challenge

class NoticeCoordinator(
    private val store: DialogStore,
    private val ui: UiHost,
    private val nowMs: () -> Long = { System.currentTimeMillis() },
) {
    companion object {
        const val PENDING_ID = "notice_pending"
        const val DISMISSED_ID = "notice_dismissed"
        const val DEBOUNCE_MS = 500L
    }

    fun onEvent(event: NoticeEvent?) {
        TODO("Implementar")
    }

    fun onHostAttached() {
        TODO("Implementar")
    }

    fun onHostDetached() {
        TODO("Implementar")
    }

    fun onUserConfirmed() {
        TODO("Implementar")
    }

    fun onUserCancelled() {
        TODO("Implementar")
    }

    fun onAppRestarted() {
        TODO("Implementar")
    }

    fun isNoticeVisible(): Boolean {
        TODO("Implementar")
    }
}
