package com.insulet.code_challenge

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

data class NoticeUiState(
    val isNoticeVisible: Boolean = false,
    val isAttached: Boolean = true,
    val isPending: Boolean = false,
    val isDismissed: Boolean = false,
    val blockingFlowActive: Boolean = false,
    val logs: List<String> = emptyList()
)

class NoticeViewModel : ViewModel(), UiHost {
    private val store = InMemoryDialogStore()
    private var _isAttached = true
    override val isAttached: Boolean
        get() = _isAttached

    var coordinator = NoticeCoordinator(store, this)
        private set

    var uiState by mutableStateOf(NoticeUiState())
        private set

    init {
        updateState("ViewModel initialized. NoticeCoordinator ready.")
    }

    override fun showNotice() {
        updateState("UiHost.showNotice() called -> Notice is VISIBLE")
    }

    override fun hideNotice() {
        updateState("UiHost.hideNotice() called -> Notice is HIDDEN")
    }

    fun onEvent(shouldShow: Boolean, blocking: Boolean = false) {
        updateState("Trigger onEvent(shouldShow = $shouldShow, blockingFlow = $blocking)")
        coordinator.onEvent(NoticeEvent(shouldShow = shouldShow, blockingFlowActive = blocking))
        refreshStatus()
    }

    fun onUserConfirmed() {
        updateState("User tapped Confirm")
        coordinator.onUserConfirmed()
        refreshStatus()
    }

    fun onUserCancelled() {
        updateState("User tapped Cancel")
        coordinator.onUserCancelled()
        refreshStatus()
    }

    fun toggleAttach() {
        _isAttached = !_isAttached
        if (_isAttached) {
            updateState("Host attached (onHostAttached())")
            coordinator.onHostAttached()
        } else {
            updateState("Host detached (onHostDetached())")
            coordinator.onHostDetached()
        }
        refreshStatus()
    }

    fun simulateAppRestart() {
        updateState("Simulating App Restart (new coordinator instance, same store)")
        coordinator = NoticeCoordinator(store, this)
        coordinator.onAppRestarted()
        refreshStatus()
    }

    private fun refreshStatus() {
        uiState = uiState.copy(
            isNoticeVisible = coordinator.isNoticeVisible(),
            isAttached = _isAttached,
            isPending = store.exists(NoticeCoordinator.PENDING_ID),
            isDismissed = store.exists(NoticeCoordinator.DISMISSED_ID)
        )
    }

    private fun updateState(message: String) {
        val newLogs = listOf(message) + uiState.logs
        uiState = uiState.copy(
            isNoticeVisible = coordinator.isNoticeVisible(),
            isAttached = _isAttached,
            isPending = store.exists(NoticeCoordinator.PENDING_ID),
            isDismissed = store.exists(NoticeCoordinator.DISMISSED_ID),
            logs = newLogs.take(25)
        )
    }
}
