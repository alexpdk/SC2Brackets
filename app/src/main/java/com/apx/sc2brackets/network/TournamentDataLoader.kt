package com.apx.sc2brackets.network

import android.util.Log
import com.apx.sc2brackets.models.Tournament
import com.apx.sc2brackets.parsers.TournamentPage
import okhttp3.OkHttpClient
import okhttp3.Request
import java.lang.Exception

private const val TAG = "TournamentDataLoader"

class TournamentDataLoader(val url: String) {

    private val okHttpClient = OkHttpClient()

    var networkResponse: NetworkResponse<String>? = null
        private set

    fun fetchPage(): NetworkResponse<String>?{
        val request = Request.Builder().url(url).build()
        okHttpClient.newCall(request).execute().use {
            networkResponse =
                NetworkResponse(it.isSuccessful, it.code(), it.body()?.string() ?: "")
        }
        return networkResponse
    }

    /**Sends network request for tournament data if result is not still obtained.
     * Parses result and returns [Tournament] on success, null on parse failure.
     * @param setAutoUpdate [Tournament.autoUpdateOn] value, that should be set in new tournament. Unfortunately,
     * without this option value is lost on update.*/
    fun loadTournament(setAutoUpdate: Boolean): Tournament?{
        if(networkResponse == null){
            fetchPage()
        }
        return networkResponse!!.let{
            if(it.isSuccessful){
                return try {
                    val tournamentPage = TournamentPage.parseHTMLContent(it.body, url)
                    Tournament(name = tournamentPage.getName(), url = url).apply {
                        matches = tournamentPage.getMatchList()
                        autoUpdateOn = setAutoUpdate
                    }
                } catch (e: Exception){
                    Log.e(TAG, "Exception during bracket parsing: ${Log.getStackTraceString(e)}")
                    null
                }
            }
            else null
        }
    }
}