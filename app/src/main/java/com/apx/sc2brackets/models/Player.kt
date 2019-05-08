package com.apx.sc2brackets.models

data class Player(val name: String, val race: Race) {
    enum class Race {
        TERRAN, PROTOSS, ZERG, TBD
    }
    companion object {
        const val TO_BE_DEFINED = "TBD"
    }
}