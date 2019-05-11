package com.apx.sc2brackets.db

import androidx.room.TypeConverter

class ScoreTypeConverter {
    @TypeConverter
    fun toOrdinal(value: Pair<Int, Int>) = value.first*100+value.second

    @TypeConverter
    fun toPair(ordinal: Int) = Pair(ordinal / 100, ordinal % 100)
}