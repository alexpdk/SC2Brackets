package com.apx.sc2brackets

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity;
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import com.apx.sc2brackets.brackets.BracketFragment
import com.apx.sc2brackets.brackets.Match

import kotlinx.android.synthetic.main.activity_main.*

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity(), BracketFragment.OnMatchInteractionListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onMatchSelect(item: Match?) {
        Log.i(TAG, "Match selected")
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**Called when the user taps the Send button. Parameter is vital for assigning method
     * as onClick handler.*/
    fun sendMessage(@Suppress("UNUSED_PARAMETER") button: View) {
        val editText = findViewById<EditText>(R.id.editText)
        val message = editText.text.toString()
        val intent = Intent(this, PlayerProfileActivity::class.java).apply {
            putExtra(PLAYER_NAME_INTENT_MESSAGE, message)
        }
        startActivity(intent)
    }

    companion object {
        const val PLAYER_NAME_INTENT_MESSAGE = "com.apx.sc2brackets.player_name"
    }

}
