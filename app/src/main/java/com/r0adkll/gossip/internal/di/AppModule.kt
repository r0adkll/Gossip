package com.r0adkll.gossip.internal.di

import com.r0adkll.gossip.AppPreferences
import com.r0adkll.gossip.arch.data.messages.FirestoreMessageRepository
import com.r0adkll.gossip.arch.data.user.FirestoreUserRepository
import com.r0adkll.gossip.arch.domain.messages.MessageRepository
import com.r0adkll.gossip.arch.domain.user.UserRepository
import com.r0adkll.gossip.arch.ui.messenger.MessengerViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    single { AppPreferences(get()) }

    single<MessageRepository> { FirestoreMessageRepository() }
    single<UserRepository> { FirestoreUserRepository(get()) }

    viewModel { MessengerViewModel(get()) }
}
