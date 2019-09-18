package com.r0adkll.gossip.arch.ui.messenger

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.r0adkll.gossip.R
import com.r0adkll.gossip.arch.ui.components.BaseFragment
import com.r0adkll.gossip.arch.ui.messenger.adapter.Item
import com.r0adkll.gossip.arch.ui.messenger.adapter.MessengerRecyclerAdapter
import org.koin.android.viewmodel.ext.android.viewModel

class MessengerFragment : BaseFragment() {

    private val viewModel by viewModel<MessengerViewModel>()
    private lateinit var adapter: MessengerRecyclerAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_messenger, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        adapter = MessengerRecyclerAdapter(requireContext()) { reply ->
            viewModel.sendTextMessage(reply)
        }

        viewModel.messages.observe(this, Observer {
            adapter.submitList(it)
        })
    }
}
