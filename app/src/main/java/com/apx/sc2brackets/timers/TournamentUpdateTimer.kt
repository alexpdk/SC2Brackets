package com.apx.sc2brackets.timers

import android.util.Log
import com.apx.sc2brackets.models.Tournament
import kotlinx.coroutines.*
import org.joda.time.DateTime
import org.joda.time.Duration
import org.joda.time.Period
import java.util.concurrent.CancellationException

private const val TAG = "TournamentUpdateTimer"

class TournamentUpdateTimer(
    private var tournament: Tournament,
    private val coroutineScope: CoroutineScope,
    private val update: TimerCallback
) {
    private var timerJob: Job = launch()
    /**Error happened during update, new tournament value not received*/
    private var updateError = false

    fun cancel() = timerJob.cancel(CancellationException("Timer cancelled"))

    private fun getPauseDuration(): Duration? {
        val updatePeriod = when (tournament.status) {
            Tournament.Status.NO_SCHEDULE -> LONG_PERIOD
            Tournament.Status.HAS_SCHEDULE -> LONG_PERIOD
            Tournament.Status.SOON -> LONG_PERIOD
            Tournament.Status.TODAY -> if (tournament.nextMatch()?.startsInHour == true) {
                SHORT_PERIOD
            } else {
                MID_PERIOD
            }
            Tournament.Status.LIVE -> SHORT_PERIOD
            Tournament.Status.ENDED_FOR_TODAY -> NO_FURTHER_UPDATE
            Tournament.Status.ENDED -> NO_FURTHER_UPDATE
        }
        return updatePeriod?.let {
            val now = DateTime.now()
            val updateTime = tournament.lastUpdate.plus(updatePeriod)
            if (updateTime.isBefore(now)) {
                Duration(0)
            } else {
                Duration(now, updateTime)
            }
        }
    }

    private fun launch() = coroutineScope.launch {
        Log.i(TAG, "Timer started for name=${tournament.name}")
        var isStopped = false
        while (!isStopped) {
            val pause = getPauseDuration()
            when {
                updateError -> {
                    delay(NETWORK_ERROR_DELAY.millis)
                    updateError = false
                }
                pause == null -> isStopped = true
                pause.standardSeconds > 1 -> delay(pause.millis)
                else -> setUpdateResult(update(tournament))
            }
        }
        Log.i(TAG, "Timer stopped for name=${tournament.name}")
    }

    fun restartFor(newTournament: Tournament) {
        Log.i(TAG, "Timer.restartFor(${tournament.name})")
        timerJob.cancel(CancellationException("Timer restarted"))
        updateError = false
        tournament = newTournament
        timerJob = launch()
    }

    private fun setUpdateResult(result: Tournament?) {
        if (result != null) {
            tournament = result
            updateError = false
        } else {
            updateError = true
        }
    }

    val tournamentURL get() = tournament.url

    companion object {
        val SHORT_PERIOD = Period.minutes(3)!!
        val MID_PERIOD = Period.minutes(30)!!
        val LONG_PERIOD = Period.hours(5)!!
        val NETWORK_ERROR_DELAY = SHORT_PERIOD.toStandardDuration()!!
        val NO_FURTHER_UPDATE: Period? = null
    }
}