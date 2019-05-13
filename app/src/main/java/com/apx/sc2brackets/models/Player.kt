package com.apx.sc2brackets.models

import androidx.room.TypeConverters
import com.apx.sc2brackets.db.RaceTypeConverter

// With Room we cannot declare fields as `val`, only as `var`
// This is error they claim to be fixed in AndroidX, but it is not fixed.
// https://stackoverflow.com/questions/44213446/cannot-find-setter-for-field-using-kotlin-with-room-database
@TypeConverters(RaceTypeConverter::class)
data class Player(var name: String = "", var race: Race = Race.TBD) {
    enum class Race {
        TERRAN, PROTOSS, ZERG, TBD
    }
    companion object {
        const val TO_BE_DEFINED = "TBD"
        val TBD = Player(name = TO_BE_DEFINED)
    }
}