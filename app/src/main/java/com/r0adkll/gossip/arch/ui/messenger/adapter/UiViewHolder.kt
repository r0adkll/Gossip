package com.r0adkll.gossip.arch.ui.messenger.adapter

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import coil.transform.CircleCropTransformation
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.r0adkll.gossip.R
import java.lang.IllegalArgumentException

sealed class UiViewHolder<I : Item>(itemView: View) : RecyclerView.ViewHolder(itemView) {

    abstract fun bind(item: I)

    sealed class MessageViewHolder<M : Item>(itemView: View) : UiViewHolder<M>(itemView) {

        protected val avatar: ImageView? = itemView.findViewById(R.id.avatar)
        protected val message = itemView.findViewById<TextView>(R.id.message)
        protected val description = itemView.findViewById<TextView>(R.id.description)

        class UserMessageViewHolder(itemView: View) : MessageViewHolder<Item.UserMessage>(itemView) {

            override fun bind(item: Item.UserMessage) {
                message.text = item.msg.value
            }
        }

        class OtherMessageViewHolder(itemView: View) : MessageViewHolder<Item.OtherMessage>(itemView) {

            override fun bind(item: Item.OtherMessage) {
                avatar?.load(item.msg.user.avatarUrl) {
                    transformations(CircleCropTransformation())
                }
                message.text = item.msg.value
            }
        }
    }

    class SmartRepliesViewHolder(
        itemView: View,
        private val onReplySelected: (String) -> Unit
    ) : UiViewHolder<Item.SmartReplies>(itemView) {

        private val chips = itemView.findViewById<ChipGroup>(R.id.chips)

        override fun bind(item: Item.SmartReplies) {
            chips.removeAllViews()
            chips.setOnCheckedChangeListener(null)

            item.replies.forEach { reply ->
                val chip = Chip(itemView.context).apply {
                    id = View.generateViewId()
                    text = reply
                }

                chip.setOnClickListener {
                    onReplySelected(reply)
                }

                chips.addView(chip)
            }
        }
    }

    private enum class ViewType(@LayoutRes val layoutId: Int) {
        USER_MESSAGE(R.layout.item_message_self),
        OTHER_MESSAGE(R.layout.item_message_other),
        SMART_REPLIES(R.layout.item_message_smart_replies);

        companion object {
            val VALUES by lazy { values() }

            fun of(@LayoutRes layoutId: Int): ViewType {
                return VALUES.find { it.layoutId == layoutId }
                    ?: throw IllegalArgumentException("Invalid layout Id")
            }
        }
    }

    companion object {

        @Suppress("UNCHECKED_CAST")
        fun create(
            itemView: View,
            viewType: Int,
            onReplySelected: (String) -> Unit
        ): UiViewHolder<Item> {
            return when (ViewType.of(viewType)) {
                ViewType.USER_MESSAGE -> MessageViewHolder.UserMessageViewHolder(itemView) as UiViewHolder<Item>
                ViewType.OTHER_MESSAGE -> MessageViewHolder.OtherMessageViewHolder(itemView) as UiViewHolder<Item>
                ViewType.SMART_REPLIES -> SmartRepliesViewHolder(itemView, onReplySelected) as UiViewHolder<Item>
            }
        }
    }
}
