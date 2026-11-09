package com.insulet.code_challenge

interface UiHost {
    val isAttached: Boolean
    fun showNotice()
    fun hideNotice()
}
