package com.r0adkll.gossip.arch.ui.messenger.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter

class MessengerRecyclerAdapter(
    context: Context,
    private val onReplySelected: (String) -> Unit
) : ListAdapter<Item, UiViewHolder<Item>>(ITEM_CALLBACK) {

    private val inflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UiViewHolder<Item> {
        val itemView = inflater.inflate(viewType, parent, false)
        return UiViewHolder.create(itemView, viewType, onReplySelected)
    }

    override fun onBindViewHolder(holder: UiViewHolder<Item>, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    companion object {
        val ITEM_CALLBACK = object : DiffUtil.ItemCallback<Item>() {

            override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean {
                return oldItem.isItemSame(newItem)
            }

            override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean {
                return oldItem.isContentSame(newItem)
            }
        }
    }
}
