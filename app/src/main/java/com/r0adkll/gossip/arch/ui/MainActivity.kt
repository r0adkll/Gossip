package com.r0adkll.gossip.arch.ui

import android.os.Bundle
import com.r0adkll.gossip.AppPreferences
import com.r0adkll.gossip.R
import com.r0adkll.gossip.arch.domain.user.UserRepository
import com.r0adkll.gossip.arch.ui.components.BaseActivity
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity : BaseActivity() {

    private val appPreferences by inject<AppPreferences>()
    private val userRepository by inject<UserRepository>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Be sure to upload our push token if we haven't already
        if (appPreferences.pushToken != null && !appPreferences.pushTokenUploaded) {
            launch {
                userRepository.updatePushToken(appPreferences.pushToken!!)
            }
        }
    }

}
