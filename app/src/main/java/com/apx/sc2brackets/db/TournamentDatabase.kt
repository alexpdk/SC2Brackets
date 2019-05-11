package com.apx.sc2brackets.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.apx.sc2brackets.models.Match
import com.apx.sc2brackets.models.TournamentEntity

@Database(entities = [TournamentEntity::class, Match::class], version = 1)
@TypeConverters(RaceTypeConverter::class)
abstract class TournamentDatabase: RoomDatabase() {
    abstract fun dao(): TournamentDao
}