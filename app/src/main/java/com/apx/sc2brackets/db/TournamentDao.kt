package com.apx.sc2brackets.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.apx.sc2brackets.models.Tournament

@Dao
interface TournamentDao {
    @Query("SELECT * FROM tournament")
    fun getAll(): LiveData<List<Tournament>>

    @Query("SELECT * FROM tournament WHERE url = :url")
    fun get(url: String): Tournament?

    @Insert
    fun insert(vararg tournaments: Tournament)

    @Transaction
    fun resetData(tournaments: List<Tournament>){
        deleteAll()
        insert(*tournaments.toTypedArray())
    }

    @Delete
    fun delete(tournament: Tournament)

    @Query("DELETE FROM tournament")
    abstract fun deleteAll()
}