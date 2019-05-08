package com.apx.sc2brackets.models

import org.joda.time.*
import org.joda.time.format.DateTimeFormat
import java.util.*
import com.apx.sc2brackets.models.Player.Race.*
import org.jetbrains.annotations.TestOnly

data class Match(val firstPlayer: Player, val secondPlayer: Player, val category: String) : MatchBracket.BracketItem {

    @TestOnly
    /*created and used only for testing purposes*/
    constructor(firstPlayer: String, secondPlayer: String, category: String): this(
        Player(firstPlayer, TBD), Player(secondPlayer, TBD), category
    )
    @TestOnly
    /*created and used only for testing purposes*/
    constructor(firstPlayer: String, firstRace: Player.Race, secondPlayer: String, secondRace: Player.Race, category: String): this(
        Player(firstPlayer, firstRace), Player(secondPlayer, secondRace), category
    )
    var score = Pair(0, 0)

    // Null time means match is to be scheduled in the future
    var startTime: DateTime? = null

    /** Are match details expanded inside list. Temporary solution for implementing expand/collapse animation*/
    var detailsExpanded = false

    fun isAfter(moment: DateTime) = startTime?.isAfter(moment) ?: true
    fun isBefore(moment: DateTime) = startTime?.isBefore(moment) ?: false

    //TODO: use Period API properly
    val countDown
        get(): String {
            if (startTime == null) {
                return "Unknown"
            }
            val now = DateTime.now()
            if (startTime!!.isBefore(now)) {
                return "Complete"
            }
            val dd = Days.daysBetween(now, startTime).days
            val hh = Hours.hoursBetween(now, startTime).hours % 24
            val mm = Minutes.minutesBetween(now, startTime).minutes % 60
            val ss = Seconds.secondsBetween(now, startTime).seconds % 60
            return when {
                dd > 0 -> "In ${dd}d ${hh}h"
                hh > 0 -> "In ${hh}h ${mm}m"
                mm > 0 -> "In ${mm}m ${ss}s"
                else -> "In ${ss}s"
            }
        }
    var maps = MatchMap.DEFAULT_MAPS

    val startTimeString get() = startTime?.let { timeFormatter.print(it) } ?: if(score == Pair(0, 0)){
        "Match is not scheduled"
    }else{
        "TIme is unknown"
    }

    companion object {
        private val PLAYERS = listOf(
            Player("Maru", TERRAN),
            Player("Serral", ZERG),
            Player("SpeCial", TERRAN),
            Player("Trap", PROTOSS),
            Player("PtitDrogo", TERRAN),
            Player("KingCobra", PROTOSS),
            Player("Kelazhur", TERRAN),
            Player("Scarlett", ZERG),
            Player("Neeb", PROTOSS))

        fun random(category: String = "Custom games"): Match {
            val pair = PLAYERS.shuffled().take(2)
            return Match(pair[0], pair[1], category)
        }

        val timeFormatter = DateTimeFormat.forPattern("MMMM d, YYYY - HH:mm z").withLocale(Locale.ENGLISH)!!
    }
}