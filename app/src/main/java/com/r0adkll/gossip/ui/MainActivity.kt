package com.r0adkll.gossip.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.r0adkll.gossip.R
import com.r0adkll.gossip.ui.components.BaseActivity

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

}
