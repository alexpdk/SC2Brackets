package com.apx.sc2brackets.activities

import android.content.Intent
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.apx.sc2brackets.R
import com.apx.sc2brackets.models.Player

import kotlinx.android.synthetic.main.activity_brackets.*
import kotlinx.android.synthetic.main.content_main.*
import android.graphics.Color
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.apx.sc2brackets.FinishAlert
import com.apx.sc2brackets.SC2BracketsApplication
import com.apx.sc2brackets.adapters.BracketFragmentPagerAdapter
import com.apx.sc2brackets.brackets.BracketFragment
import com.apx.sc2brackets.models.MatchBracket
import com.apx.sc2brackets.models.Tournament
import com.apx.sc2brackets.view_models.TournamentViewModel
import kotlinx.coroutines.Dispatchers
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
    private lateinit var tournamentViewModel: TournamentViewModel

    init {
        // allocate enough space to transfer match ViewHolders from one BracketFragment to another
        sharedRecycledViewPool.setMaxRecycledViews(R.layout.match_item, 15)
    }

    override fun onBackPressed() {
        if(suggestSave){
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
        }else{
            super.onBackPressed()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_brackets)
        setSupportActionBar(toolbar)
        suggestSave = intent.getBooleanExtra(MainActivity.SUGGEST_SAVE, false)

        tournamentViewModel = getViewModel(savedInstanceState)
        Log.i(TAG, "activity viewModel tournament = ${tournamentViewModel.tournament.value} ")
        if (!tournamentViewModel.wasNetworkRequestPerformed) {
            progressBar.visibility = VISIBLE
            Log.i(TAG, "progress bar visibility on")
            tournamentViewModel.loadBracketAsync()
        }

        bracketViewPager.adapter =
            BracketFragmentPagerAdapter(sharedRecycledViewPool, supportFragmentManager)
        bracketTabLayout.setupWithViewPager(bracketViewPager)

        progressBar.isIndeterminate = true
        val indeterminateDrawable = progressBar.indeterminateDrawable.mutate()
        indeterminateDrawable.setColorFilter(Color.rgb(25, 118, 210), android.graphics.PorterDuff.Mode.SRC_IN)
        progressBar.indeterminateDrawable = indeterminateDrawable

        fab.setOnClickListener { view ->
            selectedPlayer?.let {
                lookupPlayer(it)
            } ?: Snackbar.make(view, "Click icon near player to select", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
        fab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_person_disabled))

        tournamentViewModel.networkResponse.observe(this, Observer {
            it?.apply {
                if (!isSuccessful) {
                    showPageNotFoundAlert()
                }
            }
        })
        tournamentViewModel.bracket.observe(this, Observer {
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
        tournamentViewModel.tournament.value?.let {
            outState?.putString(TOURNAMENT_URL_STATE, it.url)
            if (it.name != null) {
                outState?.putString(TOURNAMENT_NAME_STATE, it.name)
            }
        }
        if (outState == null || tournamentViewModel.tournament.value == null) {
            Log.e(TAG, "Cannot persist open tournament")
        }
        super.onSaveInstanceState(outState)
    }

    private fun getViewModel(savedInstanceState: Bundle?): TournamentViewModel {
        val tournamentViewModel = ViewModelProviders.of(this).get(TournamentViewModel::class.java)
        Log.i(TAG, "viewModel tournament = ${tournamentViewModel.tournament.value}")

        when {
            //Activity open when returning back from child activity, ViewModel persisted
            tournamentViewModel.tournament.value != null -> Unit
            //Activity open to show tournament bracket
            intent.hasExtra(MainActivity.TOURNAMENT_URL) -> {
                val tournament = Tournament(
                    name = intent.getStringExtra(MainActivity.TOURNAMENT_NAME),
                    url = intent.getStringExtra(MainActivity.TOURNAMENT_URL)
                )
                tournamentViewModel.setNewTournament(tournament)

            }
            // Activity was deleted and ViewModel lost, restoring from savedInstanceState
            savedInstanceState != null -> {
                val tournament = Tournament(
                    name = savedInstanceState.getString(TOURNAMENT_NAME_STATE),
                    url = savedInstanceState.getString(TOURNAMENT_URL_STATE)!!
                )
                tournamentViewModel.setNewTournament(tournament)
            }
            else -> throw IllegalStateException("Activity open with no tournament referenced")
        }
        return tournamentViewModel
    }

    private fun lookupPlayer(player: Player) {
        val intent = Intent(this, PlayerProfileActivity::class.java).apply {
            putExtra(PLAYER_NAME_INTENT_MESSAGE, player.name)
            putExtra(PLAYER_RACE_INTENT_MESSAGE, player.race)
        }
        startActivity(intent)
    }

    private fun saveTournament() = GlobalScope.launch(Dispatchers.IO){
        val dao = (applicationContext as SC2BracketsApplication).tournamentDatabase.tournamentDao()
        tournamentViewModel.tournament.value?.let {
            if(dao.get(it.url) == null){
                dao.insert(it)
            }
            Log.i(TAG, "value inserted")
        }
    }

    private fun showDataNotAvailableAlert() =
        FinishAlert(this, "Sorry, cannot extract useful data from this page").show()

    private fun showPageNotFoundAlert() = FinishAlert(this, "Cannot load this web page from the site").show()

    companion object {
        const val PLAYER_NAME_INTENT_MESSAGE = "com.apx.sc2brackets.player_name"
        const val PLAYER_RACE_INTENT_MESSAGE = "com.apx.sc2brackets.player_race"

        private const val TOURNAMENT_NAME_STATE = "TOURNAMENT_NAME_STATE"
        private const val TOURNAMENT_URL_STATE = "TOURNAMENT_URL_STATE"
    }
}
