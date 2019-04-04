package com.apx.sc2brackets.brackets

import org.joda.time.DateTime

data class Match(val firstPlayer: String, val secondPlayer: String, val category: String) : MatchBracket.BracketItem {
    var races = Pair(Race.PROTOSS, Race.ZERG)
    var score = Pair(0, 0)
    var type = Type.BO7
    var startTime = DateTime().plusHours(1)!!

    /** Are match details expanded inside list. Temporary solution for implementing expand/collapse animation*/
    var detailsExpanded = false

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