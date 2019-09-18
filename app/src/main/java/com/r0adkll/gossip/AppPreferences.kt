package com.r0adkll.gossip

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.ftinc.kit.extensions.Preferences
import com.ftinc.kit.extensions.Preferences.StringPreference

class AppPreferences(context: Context) : Preferences {

    override val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    var pushToken by StringPreference(KEY_PUSH_TOKEN)
    var pushTokenUploaded by Preferences.BooleanPreference(KEY_PUSH_TOKEN_UPLOADED, false)

    companion object {
        private const val KEY_PUSH_TOKEN = "Gossip.PushToken"
        private const val KEY_PUSH_TOKEN_UPLOADED = "Gossip.PushToken.Uploaded"
    }
}
