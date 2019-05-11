package com.apx.sc2brackets.network

import android.util.Log
import com.apx.sc2brackets.models.NetworkResponse
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

    var wasNetworkRequestPerformed: Boolean = false
        private set

    fun fetchPage(): NetworkResponse<String>?{
        val request = Request.Builder().url(url).build()
        wasNetworkRequestPerformed = true
        okHttpClient.newCall(request).execute().use {
            networkResponse = NetworkResponse(it.isSuccessful, it.code(), it.body()?.string() ?: "")
        }
        return networkResponse
    }

    fun loadTournament(): Tournament?{
        if(networkResponse == null){
            fetchPage()
        }
        return networkResponse!!.let{
            if(it.isSuccessful){
                return try {
                    val tournamentPage = TournamentPage.parseHTMLContent(it.body, url)
                    Tournament(name = tournamentPage.getName(), url = url).apply {
                        matches = tournamentPage.getMatchList()
                    }
                } catch (e: Exception){
                    Log.e(TAG, "Exception during bracket parsing: ${e.stackTrace}")
                    null
                }
            }
            else null
        }
    }
}