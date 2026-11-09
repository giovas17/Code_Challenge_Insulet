package com.insulet.code_challenge

interface DialogStore {
    fun save(id: String)
    fun delete(id: String)
    fun exists(id: String): Boolean
}
