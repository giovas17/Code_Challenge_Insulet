package com.insulet.code_challenge

data class NoticeEvent(
    val shouldShow: Boolean,
    val blockingFlowActive: Boolean = false,
)
