package com.r0adkll.gossip.arch.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.r0adkll.gossip.R

class RouteActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            // Choose authentication providers
            val providers = arrayListOf(
                AuthUI.IdpConfig.GoogleBuilder().build()
            )

            // Create and launch sign-in intent
            startActivityForResult(
                AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(providers)
                    .setTheme(R.style.Theme_Gossip_Splash)
                    .setLogo(R.drawable.ic_launcher_foreground)
                    .setAlwaysShowSignInMethodScreen(true)
                    .build(),
                RC_SIGN_IN)
        } else {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN && resultCode == RESULT_OK) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    companion object {
        private const val RC_SIGN_IN = 100
    }
}
