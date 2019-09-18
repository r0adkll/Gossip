package com.r0adkll.gossip.arch.ui.messenger

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import com.ftinc.kit.extensions.dip
import com.ftinc.kit.extensions.dp
import com.ftinc.kit.extensions.int
import com.r0adkll.gossip.R
import com.r0adkll.gossip.arch.ui.components.BaseFragment
import com.r0adkll.gossip.arch.ui.messenger.adapter.Item
import com.r0adkll.gossip.arch.ui.messenger.adapter.MessengerRecyclerAdapter
import kotlinx.android.synthetic.main.fragment_messenger.*
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
        recycler.adapter = adapter
        recycler.layoutManager = LinearLayoutManager(requireContext(), VERTICAL, true)

        inputLayout.setEndIconOnClickListener {
            val message = inputMessage.text?.toString()
            if (message != null) {
                viewModel.sendTextMessage(message)
                inputMessage.text?.clear()
            } else {
                ObjectAnimator.ofFloat(inputLayout, "translationX", -dp(4f), dp(4f), 0f).apply {
                    duration = int(android.R.integer.config_shortAnimTime).toLong()
                    start()
                }
            }
        }

        viewModel.messages.observe(this, Observer {
            adapter.submitList(it)
        })
    }
}
