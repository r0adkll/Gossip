package com.r0adkll.gossip.arch.ui

import android.os.Bundle
import com.r0adkll.gossip.R
import com.r0adkll.gossip.arch.ui.components.BaseActivity

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

}
