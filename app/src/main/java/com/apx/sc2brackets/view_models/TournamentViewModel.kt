package com.apx.sc2brackets.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.apx.sc2brackets.models.MatchBracket
import com.apx.sc2brackets.models.NetworkResponse
import com.apx.sc2brackets.models.Tournament
import com.apx.sc2brackets.parsers.TournamentPage
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.lang.Exception
import java.util.concurrent.CancellationException

private const val TAG = "TournamentViewModel"

class TournamentViewModel : ViewModel(), CoroutineScope by CoroutineScope(Dispatchers.IO) {
    init{
        Log.i(TAG, "New ViewModel created")
    }

    private val _bracket = MutableLiveData<MatchBracket>()
    val bracket: LiveData<MatchBracket> get() = _bracket

    private val okHttpClient = OkHttpClient()

    private val _response = MutableLiveData<NetworkResponse<String>>()
    val networkResponse: LiveData<NetworkResponse<String>> get() = _response
    var wasNetworkRequestPerformed = false
        private set

    //tournament also can be changed, when URL in entered by user and name is retrieved from the net
    private val _tournament = MutableLiveData<Tournament>()
    val tournament: LiveData<Tournament> get() = _tournament

    private suspend fun fetchPage() = withContext(coroutineContext) {
        val request = Request.Builder()
            .url(tournament.value!!.url)
            .build()
        wasNetworkRequestPerformed = true
        okHttpClient.newCall(request).execute().use {
            val networkResponse =
                NetworkResponse(it.isSuccessful, it.code(), it.body()?.string() ?: "")
            _response.postValue(networkResponse)
            return@withContext networkResponse
        }
    }

    /**Load and parse tournament bracket from provided [Tournament::url]. Response to the bracket request is available
     * via [networkResponse].*/
    // TODO: add mutex to avoid simultaneous requests
    fun loadBracketAsync() = async {
        fetchPage().let {
            if (it.isSuccessful) {
                var newName: String? = null
                val loadedBracket = try {
                    val tournamentPage = parsePage(it.body)
                    newName = tournamentPage.getName()
                    MatchBracket(tournamentPage.getFullBracket())
                } catch (e: Exception){
                    Log.e(TAG, "Exception during bracket parsing: $e")
                    MatchBracket(emptyList())
                }
                _tournament.postValue(_tournament.value?.copy(name = newName))
                _bracket.postValue(loadedBracket)
            }
        }
    }

    private suspend fun parsePage(pageContent: String) = withContext(coroutineContext) {
        TournamentPage.parseHTMLContent(pageContent, _tournament.value!!.url)
    }

    /**Set new tournament, invalidate all data related to previous tournament*/
    fun setNewTournament(argTournament: Tournament) {
        Log.i(TAG, "set tournament = $argTournament")

        _tournament.value = argTournament
        //clear bracket, show "Loading match data..." headers
        _bracket.value = MatchBracket.LOADING_BRACKET_STUB
        if(_response.hasActiveObservers()){
            Log.e(TAG, "cannot nullify network response, observer still exists")
        }else{
            _response.value = null
        }
        wasNetworkRequestPerformed = false
    }

    override fun onCleared() {
        super.onCleared()
        cancel(CancellationException("TournamentViewModel cleared"))
        Log.i(TAG, "ViewModel cleared")
    }
}