package com.apx.sc2brackets.timers

import android.util.Log
import com.apx.sc2brackets.models.Tournament
import kotlinx.coroutines.CoroutineScope

private const val TAG = "TournamentTimerList"

typealias TimerCallback = suspend (Tournament) -> Tournament?

class TournamentTimerList(private val scope: CoroutineScope, private val callback: TimerCallback) {

    private lateinit var timers: MutableList<TournamentUpdateTimer>

    fun init(tournaments: List<Tournament>) {
        timers = tournaments.mapNotNull {
            if (it.autoUpdateOn)
                TournamentUpdateTimer(it, scope, callback)
            else null
        }
            .toMutableList()
    }

    fun addTimerFor(tournament: Tournament) {
        if (timers.find { it.tournamentURL == tournament.url } == null) {
            Log.i(TAG, "added timer for url=${tournament.url}")
            timers.add(TournamentUpdateTimer(tournament, scope, callback))
        }
    }

    fun removeTimerFor(tournament: Tournament) {
        val index = timers.indexOfFirst { it.tournamentURL == tournament.url }
        if(index >=0){
            Log.i(TAG, "removed timer for url=${tournament.url}")
            timers.removeAt(index).cancel()
        }
    }
    //restart()
}