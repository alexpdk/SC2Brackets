package com.apx.sc2brackets.models

data class MatchMap(
    val name: String,
    var crossedBy: Result = Result.NONE, var winner: Result = Result.NONE
) {

    enum class Result {
        FIRST, SECOND, NONE;
        fun isFirst() = this == FIRST
        fun isSecond() = this == SECOND
    }

    companion object {
        val DEFAULT_MAPS = listOf(
            MatchMap(
                name = "Cyber Forest",
                winner = Result.FIRST
            ),
            MatchMap(
                name = "New Repugnancy",
                winner = Result.SECOND
            ),
            MatchMap(
                name = "Port Aleksander",
                winner = Result.FIRST
            )
//            MatchMap(name = "Kairos Junction", winner = Result.SECOND)
        )
    }
}