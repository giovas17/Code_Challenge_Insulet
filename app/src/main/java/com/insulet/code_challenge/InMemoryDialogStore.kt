package com.insulet.code_challenge

class InMemoryDialogStore : DialogStore {
    private val ids = mutableSetOf<String>()

    override fun save(id: String) {
        ids.add(id)
    }

    override fun delete(id: String) {
        ids.remove(id)
    }

    override fun exists(id: String): Boolean = id in ids
}
