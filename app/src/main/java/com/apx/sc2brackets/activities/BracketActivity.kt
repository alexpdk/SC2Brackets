package com.apx.sc2brackets.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.apx.sc2brackets.R
import com.apx.sc2brackets.models.Player

import kotlinx.android.synthetic.main.activity_brackets.*
import kotlinx.android.synthetic.main.content_main.*
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.apx.sc2brackets.components.FinishAlert
import com.apx.sc2brackets.components.IndeterminateProgressBar
import com.apx.sc2brackets.SC2BracketsApplication
import com.apx.sc2brackets.adapters.BracketFragmentPagerAdapter
import com.apx.sc2brackets.MyResourceLoader
import com.apx.sc2brackets.models.MatchBracket
import com.apx.sc2brackets.view_models.BracketViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import android.app.Activity
import androidx.core.content.ContextCompat
import com.apx.sc2brackets.network.NetworkResponse
import com.google.android.material.snackbar.Snackbar

private const val TAG = "BracketActivity"

class BracketActivity : AppCompatActivity() {

    private lateinit var bracketViewModel: BracketViewModel

    /**Class providing access to Drawable, Color and other resources*/
    val resourceLoader = MyResourceLoader(this)
    /**Pool shared by all the BracketFragments.
     *
     * Without the pool the performance significantly drops due to repeated ViewHolder creation.
     * Shared view pool references activity context, so it should live inside activity to avoid memory leak*/
    val sharedRecycledViewPool = androidx.recyclerview.widget.RecyclerView.RecycledViewPool().apply {
        // allocate enough space to transfer match ViewHolders from one BracketFragment to another
        setMaxRecycledViews(R.layout.match_item, 15)
    }
    /**Suggest user to save tournament in database when returning to previous activity*/
    private var suggestSaveAtTheEnd = false

    override fun onBackPressed() {
        if (suggestSaveAtTheEnd) {
            AlertDialog.Builder(this, R.style.Theme_AppCompat_Dialog_MinWidth)
                .setMessage("Would you like to save this tournament in your list?")
                .setPositiveButton("Save") { _, _ ->
                    bracketViewModel.saveTournament(bracketViewModel.tournament.value)
                    val returnIntent = Intent()
                    returnIntent.putExtra(TOURNAMENT_URL, bracketViewModel.tournament.value?.url)
                    setResult(Activity.RESULT_OK, returnIntent)
                    finish()
                }
                .setNegativeButton("Discard") { _, _ ->
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                }
                .setCancelable(false)
                .show()
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_brackets)
        setSupportActionBar(toolbar)

        supportActionBar?.apply {
            setLogo(
                ContextCompat.getDrawable(this@BracketActivity, R.mipmap.ic_launcher)
            )
            title = "Bracket"
            setDisplayShowHomeEnabled(true)
            setDisplayUseLogoEnabled(true)
        }

        retrieveTournamentData(savedInstanceState)

        bracketViewPager.adapter =
            BracketFragmentPagerAdapter(supportFragmentManager)
        bracketTabLayout.setupWithViewPager(bracketViewPager)

        IndeterminateProgressBar(progressBar).setup()
        fab.setOnClickListener {
            progressBar.visibility = VISIBLE
            bracketViewModel.updateTournament()
        }

        bracketViewModel.tournament.observe(this, Observer {
            if (it != null) {
                if (bracketViewModel.networkResponse.value?.handler != NetworkResponse.Handler.AUTO_UPDATE) {
                    bracketViewModel.restartTimer(it)
                }
            }
        })
        bracketViewModel.networkResponse.observe(this, Observer {
            it?.apply {
                if (it.handler == NetworkResponse.Handler.AUTO_UPDATE) {
                    showAutoUpdateResult(success = it.isSuccessful)
                } else if (!isSuccessful) {
                    alerts.showPageNotFoundAlert()
                }
            }
        })
        bracketViewModel.bracket.observe(this, Observer {
            if (it != null && !it.isLoading) {
                progressBar.visibility = GONE
                if (it.isEmpty()) {
                    alerts.showDataNotAvailableAlert()
                    return@Observer
                }
                with(BracketFragmentPagerAdapter) {
                    //switch to the first tab with non-empty list (with more than only single header item)
                    bracketViewPager.setCurrentItem(/*item = */when {
                        it.filter(MatchBracket.TimeFilter.TODAY).size > 1 -> TODAY_TAB_INDEX
                        it.filter(MatchBracket.TimeFilter.NEXT).size > 1 -> FUTURE_TAB_INDEX
                        it.filter(MatchBracket.TimeFilter.PAST).size > 1 -> PAST_TAB_INDEX
                        else -> TODAY_TAB_INDEX
                    }, true
                    )
                }
            }
        })
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        Log.i(TAG, "onSaveInstanceState called")
        bracketViewModel.tournament.value?.let {
            outState?.putString(TOURNAMENT_URL, it.url)
            outState?.putBoolean(SUGGEST_SAVE, suggestSaveAtTheEnd)
            if (it.name != null) {
                outState?.putString(TOURNAMENT_NAME, it.name)
            }
        }
        if (outState == null || bracketViewModel.tournament.value == null) {
            Log.e(TAG, "Cannot persist open tournament")
        }
        super.onSaveInstanceState(outState)
    }

    private fun retrieveTournamentData(savedInstanceState: Bundle?) {
        bracketViewModel = ViewModelProviders.of(this).get(BracketViewModel::class.java)
        val database = (applicationContext as SC2BracketsApplication).tournamentDatabase

        when {
            //Activity open when returning back from child activity, ViewModel persisted
            bracketViewModel.tournament.value != null -> Unit
            //Activity open to show tournament bracket
            intent.hasExtra(TOURNAMENT_URL) -> {
                suggestSaveAtTheEnd = intent.getBooleanExtra(SUGGEST_SAVE, false)
                bracketViewModel.setDatabase(
                    database = database,
                    syncWithDatabase = !suggestSaveAtTheEnd
                )
                loadTournamentData(intent.getStringExtra(TOURNAMENT_URL))
            }
            // Activity was deleted and ViewModel lost, restoring from savedInstanceState
            savedInstanceState != null -> {
                suggestSaveAtTheEnd = savedInstanceState.getBoolean(SUGGEST_SAVE, false)
                bracketViewModel.setDatabase(
                    database = database,
                    syncWithDatabase = !suggestSaveAtTheEnd
                )
                savedInstanceState.getString(TOURNAMENT_URL)?.let {
                    loadTournamentData(it)
                }
            }
            else -> throw IllegalStateException("Activity open with no tournament referenced")
        }
    }

    private fun loadTournamentData(tournamentURL: String) {
        val async = bracketViewModel.getTournamentDataAsync(tournamentURL)
        if (suggestSaveAtTheEnd) {
            progressBar.visibility = VISIBLE
        }
        //avoid suggesting to save tournament if it is already known
        GlobalScope.launch {
            val urlUsed = async.await()
            if (urlUsed) {
                suggestSaveAtTheEnd = false
            }
        }
    }

    fun lookupPlayer(player: Player) {
        val intent = Intent(this, PlayerProfileActivity::class.java).apply {
            putExtra(PLAYER_NAME_INTENT_MESSAGE, player.name)
            putExtra(PLAYER_RACE_INTENT_MESSAGE, player.race)
        }
        startActivity(intent)
    }

    private fun showAutoUpdateResult(success: Boolean) {
        Snackbar.make(
            include, if (success) {
                "Auto-update complete"
            } else {
                "Auto-update failed"
            }, Snackbar.LENGTH_LONG
        )
            .show()
    }

    inner class Alerts(private val activity: Activity){
        private var displayed = false

        fun showDataNotAvailableAlert() {
            if(!displayed){
                displayed = true
                bracketViewModel.stopTimer()
                FinishAlert(activity, "Sorry, cannot extract useful data from this page").show()
            }
        }

        fun showPageNotFoundAlert() {
            if(!displayed){
                displayed = true
                bracketViewModel.stopTimer()
                FinishAlert(activity,"Cannot load this web page from the site").show()
            }
        }
    }
    private val alerts = Alerts(this)

    companion object {
        const val PLAYER_NAME_INTENT_MESSAGE = "com.apx.sc2brackets.player_name"
        const val PLAYER_RACE_INTENT_MESSAGE = "com.apx.sc2brackets.player_race"

        const val TOURNAMENT_NAME = "TOURNAMENT_NAME"
        const val TOURNAMENT_URL = "TOURNAMENT_URL"
        const val SUGGEST_SAVE = "SUGGEST_SAVE"
    }
}
