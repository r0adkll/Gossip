package com.r0adkll.gossip.ui.messenger

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.r0adkll.gossip.R
import com.r0adkll.gossip.ui.components.BaseFragment

class MessengerFragment : BaseFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_messenger, container, false)
    }
}
