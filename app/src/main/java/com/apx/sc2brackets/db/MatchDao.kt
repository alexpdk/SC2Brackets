/*
package com.apx.sc2brackets.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.apx.sc2brackets.models.Match


@Dao
interface MatchDao {
    @Query("SELECT * FROM `Match`")
    fun getAll(): LiveData<List<Match>>

    @Query("SELECT * FROM `Match` WHERE uuid = :uuid")
    fun get(uuid: String): Match?

    @Insert
    fun insert(vararg matches: Match)

    @Transaction
    fun resetData(matches: List<Match>){
        deleteAll()
        insert(*matches.toTypedArray())
    }

    @Delete
    fun delete(match: Match)

    @Query("DELETE FROM `Match`")
    fun deleteAll()
}*/
