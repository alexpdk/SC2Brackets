package com.apx.sc2brackets

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class PlayerProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player_profile)

        // Get the Intent that started this activity and extract the string
        val message = intent.getStringExtra(MainActivity.PLAYER_NAME_INTENT_MESSAGE)

        // Capture the layout's TextView and set the string as its text
        findViewById<TextView>(R.id.text_player_profile_header).apply {
            text = message
        }

    }

}
