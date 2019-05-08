package com.apx.sc2brackets

import android.app.Application
import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.apx.sc2brackets.models.Tournament
import com.apx.sc2brackets.db.TournamentDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

private const val TAG = "SC2BracketsApp"

class SC2BracketsApplication: Application() {

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

    inner class PopulateCallback : RoomDatabase.Callback(){
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            resetDbData()
        }

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
//            resetDbData()
        }

        private fun resetDbData(){
            // calling right now will produce "getDatabase called recursively" error
            GlobalScope.launch(Dispatchers.IO) {
                Log.i(TAG, "Calling resetData")
                tournamentDatabase.tournamentDao().resetData(Tournament.DEFAULT_KNOWN_LIST)
                Log.i(TAG, "resetData performed")
            }
        }
    }
}

