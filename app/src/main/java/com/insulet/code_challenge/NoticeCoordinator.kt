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
        // Implement code here
    }

    fun onHostAttached() {
        // Implement code here
    }

    fun onHostDetached() {
        // Implement code here
    }

    fun onUserConfirmed() {
        // Implement code here
    }

    fun onUserCancelled() {
        // Implement code here
    }

    fun onAppRestarted() {
        // Implement code here
    }

    fun isNoticeVisible(): Boolean {
        return false
    }
}
