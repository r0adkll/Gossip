package com.r0adkll.gossip.arch.ui.messenger

import androidx.lifecycle.*
import com.google.firebase.auth.FirebaseAuth
import com.r0adkll.gossip.R
import com.r0adkll.gossip.arch.domain.messages.Message
import com.r0adkll.gossip.arch.domain.messages.MessageRepository
import com.r0adkll.gossip.arch.ui.messenger.adapter.Item
import com.r0adkll.gossip.extensions.combineLatest
import com.r0adkll.gossip.extensions.doOnNext
import com.r0adkll.gossip.util.Event
import com.r0adkll.gossip.util.wrapEspressoIdlingResource
import kotlinx.coroutines.launch
import timber.log.Timber

class MessengerViewModel(private val messageRepository: MessageRepository) : ViewModel() {

    val messages: LiveData<List<Item>>

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<Event<Int>>()
    val error: LiveData<Event<Int>> = _error

    private val _smartReplies = MutableLiveData<List<String>>(emptyList())
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    init {
        messages = messageRepository.observeMessages()
            .doOnNext {
                getSmartReplies(it)
            }
            .combineLatest(_smartReplies) { messages, smartReplies ->
                val items = ArrayList<Item>()

                Timber.d("Combining Messages($messages) and SmartReplies($smartReplies)")

                items += messages.sortedByDescending { it.createdAt }
                    .map {
                        if (it.user.id == userId) {
                            Item.UserMessage(it)
                        } else {
                            Item.OtherMessage(it)
                        }
                    }

                if (smartReplies.isNotEmpty()) {
                    items += Item.SmartReplies(smartReplies)
                }

                items
            }
    }

    fun sendTextMessage(text: String) {
        sendMessage(Message.text(text))
    }

    fun sendGifMessage(url: String) {
        sendMessage(Message.gif(url))
    }

    private fun sendMessage(message: Message) {
        _loading.value = true
        _smartReplies.value = emptyList()
        wrapEspressoIdlingResource {
            viewModelScope.launch {
                val result = messageRepository.postMessage(message)
                if (result.isSuccess) {
                    Timber.i("Message(${result.getOrNull()}) posted!")
                    _error.value = null
                } else {
                    Timber.e(result.exceptionOrNull(), "Unable to post message '$message'")
                    _error.value = Event(R.string.posting_message_error)
                }
                _loading.value = false
            }
        }
    }

    private fun getSmartReplies(messages: List<Message>) {
        Timber.d("Fetch smart replies for $messages")
        wrapEspressoIdlingResource {
            viewModelScope.launch {
                val result = messageRepository.getSmartReplies(messages)
                if (result.isSuccess) {
                    Timber.i("Smart replies fetched!")
                    _smartReplies.value = result.getOrNull() ?: emptyList()
                    _error.value = null
                } else {
                    _smartReplies.value = emptyList()
                    Timber.e(result.exceptionOrNull(), "Unable to fetch smart replies")
                    _error.value = Event(R.string.posting_message_error)
                }
            }
        }
    }
}
