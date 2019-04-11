package com.apx.sc2brackets.brackets

import org.joda.time.*

data class Match(val firstPlayer: String, val secondPlayer: String, val category: String) : MatchBracket.BracketItem {
    var races = Pair(Race.PROTOSS, Race.ZERG)
    var score = Pair(0, 0)
    var type = Type.BO7

    // Null time means match is to be scheduled in the future
    var startTime: DateTime? = null

    /** Are match details expanded inside list. Temporary solution for implementing expand/collapse animation*/
    var detailsExpanded = false

    fun isAfter(moment: DateTime) = startTime?.isAfter(moment) ?: true
    fun isBefore(moment: DateTime) = startTime?.isBefore(moment) ?: false

    //TODO: use Period API properly
    val countDown get(): String{
        if(startTime == null){
            return "Unknown"
        }
        val now = DateTime.now()
        if(startTime!!.isBefore(now)){
            return "Complete"
        }
        val dd = Days.daysBetween(now, startTime).days
        val hh = Hours.hoursBetween(now, startTime).hours % 24
        val mm = Minutes.minutesBetween(now, startTime).minutes % 60
        val ss = Seconds.secondsBetween(now, startTime).seconds % 60
        return when{
            dd>0 ->"In ${dd}d ${hh}h"
            hh>0 ->"In ${hh}h ${mm}m"
            mm>0 ->"In ${mm}m ${ss}s"
            else ->"In ${ss}s"
        }
    }
    /* fun setScore(first: Int = 0, second: Int = 0): Match{
         if(first > 0 && second > 0){
             score.copy(first, second)
         }else if(first > 0){
             score.copy(first)
         }else if(second > 0){
             score.copy(second)
         }else{

         }
     }
 */
    enum class Type {
        BO1, BO3, BO5, BO7, BO9
    }

    enum class Race {
        PROTOSS, ZERG
    }

    companion object {
        private val PLAYERS = listOf("Maru", "Serral", "Special", "PtitDrogo", "Trap", "KingCobra", "Kelazhur", "Scarlett", "Neeb")

        val DEFAULT_LIST = Array(15) { i ->
            random().apply {
                score = score.copy(first = i)
            }
        }.toList()

        fun random(category: String = "Custom games"): Match {
            val pair = PLAYERS.shuffled().take(2)
            return Match(pair[0], pair[1], category)
        }
    }
}