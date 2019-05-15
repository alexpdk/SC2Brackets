package com.apx.sc2brackets.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.apx.sc2brackets.db.TournamentDao
import com.apx.sc2brackets.db.TournamentDatabase
import com.apx.sc2brackets.models.MatchBracket
import com.apx.sc2brackets.network.NetworkResponse
import com.apx.sc2brackets.models.Tournament
import com.apx.sc2brackets.network.TournamentDataLoader
import com.apx.sc2brackets.timers.TimerCallback
import com.apx.sc2brackets.timers.TournamentUpdateTimer
import kotlinx.coroutines.*
import java.util.concurrent.CancellationException

private const val TAG = "BracketViewModel"

class BracketViewModel : ViewModel(), CoroutineScope by CoroutineScope(Dispatchers.IO) {

    private lateinit var dao: TournamentDao

    private val _bracket = MutableLiveData<MatchBracket>()
    /**Current match bracket displayed in the list*/
    val bracket: LiveData<MatchBracket> get() = _bracket

    private val _response = MutableLiveData<NetworkResponse<String>>()
    /**Indicates the latest response to manual update request.
     * Used to toggle visibility of progress indicator and display network failure message to user.*/
    val networkResponse: LiveData<NetworkResponse<String>> get() = _response

    /**True, if tournament data is recorded in local database and should be updated with fresh data from the network.
     * False, if there is no tournament data stored in database (user just opened bracket of new tournament)*/
    private var syncWithDatabase = true

    /**Timer used to auto-update data on the page*/
    private var timer: TournamentUpdateTimer? = null

    //tournament also can be changed, when URL in entered by user and name is retrieved from the net
    private val _tournament = MutableLiveData<Tournament>()
    val tournament: LiveData<Tournament> get() = _tournament

    /**Loads tournament data from database or request from network if no local data is stored.
     *
     * Async job returns true if data is local, false otherwise.*/
    fun getTournamentDataAsync(tournamentURL: String) = async {
        val tournament = dao.getTournament(url = tournamentURL)
        val isLocal = tournament != null
        if (isLocal) {
            tournament.let {
                _tournament.postValue(it)
                _bracket.postValue(MatchBracket(it!!.matches))
            }
        } else {
            //Not sync with database until user manually orders this in the dialog window
            syncWithDatabase = false
            //clear bracket, show "Loading match data..." headers
            _bracket.postValue(MatchBracket.LOADING_BRACKET_STUB)
            loadBracket(tournamentURL)
        }
        return@async isLocal
    }

    /**Load and parse tournament bracket from provided [Tournament::url]. Response to the bracket request is available
     * via [networkResponse] LiveData
     *
     * @param tournamentURL url to load tournament data
     * @param isAutoUpdate should network response handler be marked as [NetworkResponse.Handler.AUTO_UPDATE]
     * @return Tournament is data successfully retrieved, null otherwise*/
    private fun loadBracket(tournamentURL: String, isAutoUpdate: Boolean = false): Tournament? {
        val loader = TournamentDataLoader(tournamentURL)
        //set network request
        _response.postValue(loader.fetchPage()?.apply {
            if(isAutoUpdate){
                setHander(NetworkResponse.Handler.AUTO_UPDATE)
            }
        })
        return if (loader.networkResponse?.isSuccessful == true) {
            val tournament = loader.loadTournament(
                //retain autoUpdate value of previous tournament
                setAutoUpdate = _tournament.value?.autoUpdateOn == true
            )
            tournament?.apply {
                _tournament.postValue(this)
                _bracket.postValue(MatchBracket(matches))
            } ?: let {
                //error happened, data load failed
                _bracket.postValue(MatchBracket.ERROR_BRACKET_STUB)
                null
            }
        } else null
    }

    private val onTimerUpdate: TimerCallback = {
        val newTournament = loadBracket(it.url, isAutoUpdate = true)
        if(syncWithDatabase){
            saveTournament(newTournament)
        }
        newTournament
    }

    /**Restarts timer performing tournament auto-update if that is enabled.
     * Stops updating run with previous tournament value, because update period depends on the current tournament status.
     *
     * Should be called inside Observer on each tournament value change.*/
    fun restartTimer(tournament: Tournament) {
        timer?.apply {
            Log.i(TAG, "timer restarted for tournament = ${tournament.name}")
            restartFor(tournament)
        } ?: let {
            Log.i(TAG, "new timer created for tournament= ${tournament.name}")
            timer = TournamentUpdateTimer(
                tournament,
                coroutineScope = this,
                update = onTimerUpdate
            )
        }
    }

    /**Inserts or updates database tournament record.
     * Launched in GlobalScope to avoid termination when activity is closed and ViewModel destroyed. */
    fun saveTournament(tournament: Tournament?) = GlobalScope.launch(Dispatchers.IO) {
        tournament?.let {
            if(dao.getTournament(it.url) == null){
                dao.insert(it)
            }else{
                dao.updateIfExists(it.url, it)
            }
        }
    }

    fun setDatabase(database: TournamentDatabase, syncWithDatabase: Boolean) {
        dao = database.dao()
        this.syncWithDatabase = syncWithDatabase
    }

    fun stopTimer(){
        timer?.cancel()
        timer = null
    }

    /**Update existing tournament data*/
    fun updateTournament() = launch {
        val url = _tournament.value?.url ?: return@launch
        loadBracket(url)?.let {
            if (syncWithDatabase) {
                saveTournament(it)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        cancel(CancellationException("BracketViewModel cleared"))
    }
}