package com.apx.sc2brackets.activities

import android.content.Intent
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
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
import com.apx.sc2brackets.FinishAlert
import com.apx.sc2brackets.IndeterminateProgressBar
import com.apx.sc2brackets.SC2BracketsApplication
import com.apx.sc2brackets.adapters.BracketFragmentPagerAdapter
import com.apx.sc2brackets.brackets.BracketFragment
import com.apx.sc2brackets.models.MatchBracket
import com.apx.sc2brackets.view_models.BracketViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

private const val TAG = "BracketActivity"

class BracketActivity : AppCompatActivity(), BracketFragment.OnMatchInteractionListener {

    /**Pool shared by all the BracketFragments.
     *
     * Without the pool the performance significantly drops due to repeated ViewHolder creation.
     * Shared view pool references activity context, so it should live inside activity to avoid memory leak*/
    private val sharedRecycledViewPool = androidx.recyclerview.widget.RecyclerView.RecycledViewPool()

    private var selectedPlayer: Player? = null
    private var suggestSave = false
    private lateinit var bracketViewModel: BracketViewModel

    init {
        // allocate enough space to transfer match ViewHolders from one BracketFragment to another
        sharedRecycledViewPool.setMaxRecycledViews(R.layout.match_item, 15)
    }

    override fun onBackPressed() {
        if (suggestSave) {
            AlertDialog.Builder(this, R.style.Theme_AppCompat_Dialog_MinWidth)
                .setMessage("Would you like to keep tournament in list?")
                .setPositiveButton("Save") { _, _ ->
                    saveTournament()
                    super.onBackPressed()
                }
                .setNegativeButton("Discard") { _, _ ->
                    super.onBackPressed()
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

        bracketViewModel = getViewModel(savedInstanceState)
        loadTournamentData()

        bracketViewPager.adapter =
            BracketFragmentPagerAdapter(sharedRecycledViewPool, supportFragmentManager)
        bracketTabLayout.setupWithViewPager(bracketViewPager)

        IndeterminateProgressBar(progressBar).setup()
        fab.setOnClickListener {
            selectedPlayer?.let {
                lookupPlayer(it)
                selectedPlayer = null
                fab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_sync_light))
            } ?: let {
                progressBar.visibility = VISIBLE
                bracketViewModel.updateTournament()
            }
        }
        fab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_sync_light))

        bracketViewModel.networkResponse.observe(this, Observer {
            it?.apply {
                if (!isSuccessful) {
                    showPageNotFoundAlert()
                }
            }
        })
        bracketViewModel.bracket.observe(this, Observer {
            if (it != null && !it.isLoading) {
                progressBar.visibility = GONE
                if (it.isEmpty()) {
                    showDataNotAvailableAlert()
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
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

    override fun onPlayerSelect(player: Player) {
        //TODO: handle deselection
        selectedPlayer = player
        fab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_person))
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        Log.i(TAG, "onSaveInstanceState called")
        bracketViewModel.tournament.value?.let {
            outState?.putString(TOURNAMENT_URL_STATE, it.url)
            outState?.putBoolean(SUGGEST_SAVE_STATE, suggestSave)
            if (it.name != null) {
                outState?.putString(TOURNAMENT_NAME_STATE, it.name)
            }
        }
        if (outState == null || bracketViewModel.tournament.value == null) {
            Log.e(TAG, "Cannot persist open tournament")
        }
        super.onSaveInstanceState(outState)
    }

    private fun getViewModel(savedInstanceState: Bundle?): BracketViewModel {
        val bracketViewModel = ViewModelProviders.of(this).get(BracketViewModel::class.java)
        val database = (applicationContext as SC2BracketsApplication).tournamentDatabase

        when {
            //Activity open when returning back from child activity, ViewModel persisted
            bracketViewModel.tournament.value != null -> Unit
            //Activity open to show tournament bracket
            intent.hasExtra(MainActivity.TOURNAMENT_URL) -> {
                bracketViewModel.setDatabaseAndURL(
                    database = database,
                    tournamentURL = intent.getStringExtra(MainActivity.TOURNAMENT_URL)
                )
                suggestSave = intent.getBooleanExtra(MainActivity.SUGGEST_SAVE, false)
            }
            // Activity was deleted and ViewModel lost, restoring from savedInstanceState
            savedInstanceState != null -> {
                bracketViewModel.setDatabaseAndURL(
                    database = database,
                    tournamentURL = savedInstanceState.getString(TOURNAMENT_URL_STATE)!!
                )
                suggestSave = savedInstanceState.getBoolean(SUGGEST_SAVE_STATE, false)
            }
            else -> throw IllegalStateException("Activity open with no tournament referenced")
        }
        return bracketViewModel
    }

    private fun loadTournamentData() {
        val async = bracketViewModel.getTournamentDataAsync(isTournamentKnown = !suggestSave)
        if (suggestSave) {
            progressBar.visibility = VISIBLE
        }
        //avoid suggesting to save tournament if it is already known
        GlobalScope.launch {
            val urlUsed = async.await()
            if(urlUsed){
                suggestSave = false
            }
        }
    }

    private fun lookupPlayer(player: Player) {
        val intent = Intent(this, PlayerProfileActivity::class.java).apply {
            putExtra(PLAYER_NAME_INTENT_MESSAGE, player.name)
            putExtra(PLAYER_RACE_INTENT_MESSAGE, player.race)
        }
        startActivity(intent)
    }

    private fun saveTournament() {
        bracketViewModel.saveTournament(isTournamentKnown = !suggestSave)
    }

    private fun showDataNotAvailableAlert() =
        FinishAlert(this, "Sorry, cannot extract useful data from this page").show()

    private fun showPageNotFoundAlert() = FinishAlert(this, "Cannot load this web page from the site").show()

    companion object {
        const val PLAYER_NAME_INTENT_MESSAGE = "com.apx.sc2brackets.player_name"
        const val PLAYER_RACE_INTENT_MESSAGE = "com.apx.sc2brackets.player_race"

        private const val TOURNAMENT_NAME_STATE = "TOURNAMENT_NAME_STATE"
        private const val TOURNAMENT_URL_STATE = "TOURNAMENT_URL_STATE"
        private const val SUGGEST_SAVE_STATE = "SUGGEST_SAVE_STATE"
    }
}
