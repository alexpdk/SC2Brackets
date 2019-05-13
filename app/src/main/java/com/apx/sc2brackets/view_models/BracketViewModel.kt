package com.apx.sc2brackets.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.apx.sc2brackets.db.TournamentDao
import com.apx.sc2brackets.db.TournamentDatabase
import com.apx.sc2brackets.models.MatchBracket
import com.apx.sc2brackets.models.NetworkResponse
import com.apx.sc2brackets.models.Tournament
import com.apx.sc2brackets.network.TournamentDataLoader
import kotlinx.coroutines.*
import java.util.concurrent.CancellationException

class BracketViewModel : ViewModel(), CoroutineScope by CoroutineScope(Dispatchers.IO) {

    private lateinit var dao: TournamentDao

    private val _bracket = MutableLiveData<MatchBracket>()
    val bracket: LiveData<MatchBracket> get() = _bracket

    private val _response = MutableLiveData<NetworkResponse<String>>()
    val networkResponse: LiveData<NetworkResponse<String>> get() = _response

    private lateinit var loader: TournamentDataLoader

    private var syncWithDatabase = true
    private var tournamentURLUsed = true

    //tournament also can be changed, when URL in entered by user and name is retrieved from the net
    private val _tournament = MutableLiveData<Tournament>()
    val tournament: LiveData<Tournament> get() = _tournament

    fun getTournamentDataAsync(isTournamentKnown: Boolean) = async {
        val tournament = dao.getTournament(url = loader.url)
        tournamentURLUsed = tournament != null
        if(isTournamentKnown){
            tournament?.let {
                _tournament.postValue(it)
                _bracket.postValue(MatchBracket(it.matches))
            }
        }else{
            //Not sync with database until user manually orders this in the dialog window
            syncWithDatabase = false
            //clear bracket, show "Loading match data..." headers
            _bracket.postValue(MatchBracket.LOADING_BRACKET_STUB)
            loadBracket()
        }
        return@async tournamentURLUsed
    }

    /**Load and parse tournament bracket from provided [Tournament::url]. Response to the bracket request is available
     * via [networkResponse].*/
    private fun loadBracket(): Tournament?{
         _response.postValue(loader.fetchPage())
        return if(loader.networkResponse?.isSuccessful == true) {
            val tournament = loader.loadTournament()
            tournament?.apply {
                _tournament.postValue(this)
                _bracket.postValue(MatchBracket(matches))
            } ?: let {
                //error happened, data load failed
                _bracket.postValue(MatchBracket.ERROR_BRACKET_STUB)
                null
            }
        }else null
    }

    //Launched in GlobalScope to avoid termination when activity is closed and ViewModel destroyed
    fun saveTournament(isTournamentKnown: Boolean) = GlobalScope.launch(Dispatchers.IO){
        _tournament.value?.let {
            if(isTournamentKnown){
                dao.updateIfExists(it.url, it)
            }else{
                //safe call required if url already occupied
                dao.getOrInsertDefault(it.url, it)
            }
        }
    }

    fun setDatabaseAndURL(database: TournamentDatabase, tournamentURL: String){
        dao = database.dao()
        loader = TournamentDataLoader(tournamentURL)
    }

    /**Update existing tournament data*/
    fun updateTournament() = launch{
        loadBracket()?.let {
            if(syncWithDatabase){
                saveTournament(isTournamentKnown = true)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        cancel(CancellationException("BracketViewModel cleared"))
    }
}