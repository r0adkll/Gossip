package com.r0adkll.gossip.arch.domain.user

interface UserRepository {

    suspend fun updatePushToken(pushToken: String)
}
