package com.apx.sc2brackets.db

import androidx.room.TypeConverter
import com.apx.sc2brackets.models.Player

class RaceTypeConverter {
    @TypeConverter
    fun toOrdinal(race: Player.Race) = race.ordinal

    @TypeConverter
    fun toRace(ordinal: Int) = Player.Race.values().first { it.ordinal == ordinal }
}