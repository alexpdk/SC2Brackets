package com.apx.sc2brackets.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.apx.sc2brackets.models.Tournament

@Database(entities = [Tournament::class], version = 1)
abstract class TournamentDatabase: RoomDatabase() {
    abstract fun tournamentDao(): TournamentDao
}