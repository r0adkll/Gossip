package com.r0adkll.gossip.arch.ui.messenger.adapter

import com.ftinc.kit.recycler.RecyclerViewItem
import com.r0adkll.gossip.R
import com.r0adkll.gossip.arch.domain.Message

sealed class Item : RecyclerViewItem {

    abstract val itemId: Long

    class UserMessage(val msg: Message) : Item() {

        override val itemId: Long get() = msg.id.hashCode().toLong()
        override val layoutId: Int get() = R.layout.item_message_self

        override fun isItemSame(new: RecyclerViewItem): Boolean = when(new) {
            is UserMessage -> new.msg.id == msg.id
            else -> false
        }

        override fun isContentSame(new: RecyclerViewItem): Boolean = when(new) {
            is UserMessage -> new.msg == msg
            else -> false
        }
    }

    class OtherMessage(val msg: Message) : Item() {

        override val itemId: Long get() = msg.id.hashCode().toLong()
        override val layoutId: Int get() = R.layout.item_message_other

        override fun isItemSame(new: RecyclerViewItem): Boolean = when(new) {
            is OtherMessage -> new.msg.id == msg.id
            else -> false
        }

        override fun isContentSame(new: RecyclerViewItem): Boolean = when(new) {
            is OtherMessage -> new.msg == msg
            else -> false
        }
    }

    class SmartReplies(val replies: List<String>) : Item() {

        override val itemId: Long get() = 0L
        override val layoutId: Int get() = R.layout.item_message_smart_replies

        override fun isItemSame(new: RecyclerViewItem): Boolean = new is SmartReplies

        override fun isContentSame(new: RecyclerViewItem): Boolean = when (new) {
            is SmartReplies -> new.replies == replies
            else -> false
        }
    }
}
