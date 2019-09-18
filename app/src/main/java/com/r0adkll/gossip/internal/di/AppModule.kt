package com.r0adkll.gossip.internal.di

import com.r0adkll.gossip.arch.data.FirestoreMessageRepository
import com.r0adkll.gossip.arch.domain.MessageRepository
import com.r0adkll.gossip.arch.ui.messenger.MessengerViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    single<MessageRepository> { FirestoreMessageRepository() }
    viewModel { MessengerViewModel(get()) }
}
