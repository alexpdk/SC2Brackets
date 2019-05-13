package com.apx.sc2brackets.db

import androidx.room.TypeConverter
import com.apx.sc2brackets.models.MatchMap
import com.apx.sc2brackets.models.Player
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import java.util.*

class DateTimeTypeConverter {
    companion object {
        val format = DateTimeFormat.forPattern("MMMM d, YYYY - HH:mm z").withLocale(Locale.ENGLISH)!!
        private val readFormat = DateTimeFormat.forPattern("MMMM d, YYYY - HH:mm").withLocale(Locale.ENGLISH)!!

        @JvmStatic
        @TypeConverter
        fun toString(dateTime: DateTime?) = dateTime?.let {
            format.print(dateTime)!!
        } ?: "null"

        @JvmStatic
        @TypeConverter
        fun toDateTime(value: String): DateTime? = if (value != "null") {
            val lastSpace = value.lastIndexOf(" ")
            val dateTime = value.substring(0 until lastSpace)
            val timeZoneDescriptor = value.substring(value.length - 6)
            readFormat.parseDateTime(dateTime).withZoneRetainFields(DateTimeZone.forID(timeZoneDescriptor))!!
        } else null
    }
}

class RaceTypeConverter {
    @TypeConverter
    fun toOrdinal(race: Player.Race) = race.ordinal

    @TypeConverter
    fun toRace(ordinal: Int) = Player.Race.values().first { it.ordinal == ordinal }
}

class ResultTypeConverter {
    @TypeConverter
    fun toOrdinal(result: MatchMap.Result) = result.ordinal

    @TypeConverter
    fun toResult(ordinal: Int) = MatchMap.Result.values().first { it.ordinal == ordinal }
}

class ScoreTypeConverter {
    @TypeConverter
    fun toOrdinal(value: Pair<Int, Int>) = value.first*100+value.second

    @TypeConverter
    fun toPair(ordinal: Int) = Pair(ordinal / 100, ordinal % 100)
}