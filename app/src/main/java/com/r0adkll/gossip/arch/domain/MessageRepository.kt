package com.r0adkll.gossip.arch.domain

import androidx.lifecycle.LiveData

interface MessageRepository {

    fun observeMessages(): LiveData<List<Message>>
    suspend fun postMessage(message: Message): Result<String>
    suspend fun getSmartReplies(messages: List<Message>): Result<List<String>>
}
