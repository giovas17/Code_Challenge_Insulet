package com.insulet.code_challenge

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class NoticeCoordinatorTest {

    private lateinit var store: InMemoryDialogStore
    private lateinit var ui: FakeUi
    private var now: Long = 0L
    private lateinit var coordinator: NoticeCoordinator

    @Before
    fun setUp() {
        store = InMemoryDialogStore()
        ui = FakeUi()
        now = 1_000L
        coordinator = NoticeCoordinator(store, ui) { now }
    }

    @Test
    fun `shows the notice the first time the condition becomes true`() {
        coordinator.onEvent(NoticeEvent(shouldShow = true))

        assertTrue(coordinator.isNoticeVisible())
        assertEquals(1, ui.showCount)
    }

    @Test
    fun `ignores a duplicate event while the notice is already visible`() {
        coordinator.onEvent(NoticeEvent(shouldShow = true))
        coordinator.onEvent(NoticeEvent(shouldShow = true))

        assertEquals(1, ui.showCount)
    }

    @Test
    fun `null event is ignored and does not crash`() {
        coordinator.onEvent(null)

        assertFalse(coordinator.isNoticeVisible())
        assertEquals(0, ui.showCount)
    }

    @Test
    fun `does not show while another blocking flow is active`() {
        coordinator.onEvent(NoticeEvent(shouldShow = true, blockingFlowActive = true))

        assertFalse(coordinator.isNoticeVisible())
        assertEquals(0, ui.showCount)
    }

    @Test
    fun `does not touch the UI when the host is detached, then shows on attach`() {
        ui.isAttached = false
        coordinator.onEvent(NoticeEvent(shouldShow = true))

        assertEquals(0, ui.showCount)
        assertFalse(coordinator.isNoticeVisible())

        ui.isAttached = true
        coordinator.onHostAttached()

        assertTrue(coordinator.isNoticeVisible())
        assertEquals(1, ui.showCount)
    }

    @Test
    fun `does not crash if the user confirms while the host is detached`() {
        coordinator.onEvent(NoticeEvent(shouldShow = true))
        ui.isAttached = false
        coordinator.onHostDetached()

        coordinator.onUserConfirmed()

        assertFalse(coordinator.isNoticeVisible())
        assertEquals(0, ui.hideCount)
    }

    @Test
    fun `does not restore after the user confirmed`() {
        coordinator.onEvent(NoticeEvent(shouldShow = true))
        coordinator.onUserConfirmed()

        val restarted = NoticeCoordinator(store, ui) { now }
        restarted.onAppRestarted()

        assertFalse(restarted.isNoticeVisible())
        assertEquals(1, ui.showCount)
    }

    @Test
    fun `restores after process death if still pending and host is attached`() {
        coordinator.onEvent(NoticeEvent(shouldShow = true))
        val restartedUi = FakeUi()
        val restarted = NoticeCoordinator(store, restartedUi) { now }

        restarted.onAppRestarted()

        assertTrue(restarted.isNoticeVisible())
        assertEquals(1, restartedUi.showCount)
    }

    @Test
    fun `clears pending state when the condition becomes false`() {
        coordinator.onEvent(NoticeEvent(shouldShow = true))
        coordinator.onEvent(NoticeEvent(shouldShow = false))

        assertFalse(coordinator.isNoticeVisible())
        assertEquals(1, ui.hideCount)

        val restarted = NoticeCoordinator(store, FakeUi()) { now }
        restarted.onAppRestarted()
        assertFalse(restarted.isNoticeVisible())
    }

    @Test
    fun `ignores a second confirm tap within the debounce window`() {
        coordinator.onEvent(NoticeEvent(shouldShow = true))
        coordinator.onUserConfirmed()
        coordinator.onUserConfirmed()

        assertEquals(1, ui.hideCount)
    }

    @Test
    fun `accepts a new confirm after the debounce window`() {
        coordinator.onEvent(NoticeEvent(shouldShow = true))
        coordinator.onUserConfirmed()

        coordinator.onEvent(NoticeEvent(shouldShow = false))
        coordinator.onEvent(NoticeEvent(shouldShow = true))
        now += NoticeCoordinator.DEBOUNCE_MS + 1
        coordinator.onUserConfirmed()

        assertEquals(2, ui.hideCount)
    }

    private class FakeUi : UiHost {
        override var isAttached: Boolean = true
        var showCount: Int = 0
        var hideCount: Int = 0

        override fun showNotice() {
            showCount++
        }

        override fun hideNotice() {
            hideCount++
        }
    }
}
