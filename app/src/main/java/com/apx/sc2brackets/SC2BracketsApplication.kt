package com.apx.sc2brackets

import android.app.Application
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.apx.sc2brackets.models.Tournament
import com.apx.sc2brackets.db.TournamentDatabase
import com.apx.sc2brackets.models.Match
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

private const val TAG = "SC2BracketsApp"

class SC2BracketsApplication : Application() {
    lateinit var tournamentDatabase: TournamentDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        tournamentDatabase = Room.databaseBuilder(
            applicationContext,
            TournamentDatabase::class.java, "sc2brackets.db"
        ).addCallback(PopulateCallback()).build()

        //dumb operation to initialize connection and trigger callback
        tournamentDatabase.query("select 1", null)
    }

    inner class PopulateCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            resetTournamentData()
        }

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
//            resetMatchData()
        }

//        private fun resetMatchData() {
//            GlobalScope.launch(Dispatchers.IO) {
//                Tournament.DEFAULT_KNOWN_LIST.forEach {
//                    val current = tournamentDatabase.dao().getOrInsertDefault(it.url, defaultValue = it)
//                    tournamentDatabase.dao().replaceMatches(
//                        foreignKey = current.primaryKey,
//                        newMatches = Array(5) { Match.random() }.toList()
//                    )
//                }
//            }
//        }

        private fun resetTournamentData() {
            // calling right now will produce "getDatabase called recursively" error
            GlobalScope.launch(Dispatchers.IO) {
                tournamentDatabase.dao().replaceTournaments(Tournament.DEFAULT_KNOWN_LIST)
            }
        }
    }
}

