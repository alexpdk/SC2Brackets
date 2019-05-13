package com.apx.sc2brackets.db

import androidx.room.*
import com.apx.sc2brackets.models.*

@Dao
interface TournamentDao {

    @Query("SELECT * FROM tournament")
    @Transaction
    fun getAllTournaments(): List<Tournament>

    @Query("SELECT * FROM `Match` WHERE id = :id")
    @Transaction
    fun getMatch(id: Int): Match?

    @Query("SELECT * FROM tournament WHERE url = :url")
    @Transaction
    fun getTournament(url: String): Tournament?

    @Query("SELECT * FROM tournament WHERE id = :id")
    @Transaction
    fun getTournament(id: Int): Tournament?

    /**If there is no tournament with specified [url] in database, insert defaultValue
     * and retrieve it back with auto-generated primary key.*/
    @Transaction
    fun getOrInsertDefault(url: String, defaultValue: Tournament): Tournament = getTournament(url) ?: run {
        insert(defaultValue)
        getTournament(defaultValue.url)!!
    }

    @Insert
    fun insert(vararg maps: MatchMap)

    @Insert
    fun insert(matchEntity: MatchEntity): Long

    @Transaction
    fun insert(vararg matches: Match){
        matches.forEach {match->
            val id = insert(match.entity)
            if(id >= 0){
                match.maps.forEach { it.matchID = id.toInt() }
                insert(*match.maps.toTypedArray())
            }
        }
    }

    @Insert
    fun insert(tournamentEntity: TournamentEntity): Long

    @Transaction
    fun insert(vararg tournaments: Tournament) {
        tournaments.forEach { tournament ->
            //will produce constraint violation if url is not unique
            getTournament(url = tournament.url)?.let {
                tournament.primaryKey = it.primaryKey
            }
            //if inserting TournamentEntity succeeded, insert matches
            val generatedPrimaryKey = insert(tournament.entity)
            if (generatedPrimaryKey >= 0) {
                tournament.matches.forEach { it.tournamentID = generatedPrimaryKey.toInt() }
                insert(*tournament.matches.toTypedArray())
            }
        }
    }

    @Transaction
    fun replaceMatches(foreignKey: Int, newMatches: List<Match>) {
        deleteMatches(foreignKey)
        newMatches.forEach { it.tournamentID = foreignKey }
        insert(*newMatches.toTypedArray())
    }

    @Transaction
    fun replaceTournaments(newTournaments: List<Tournament>) {
        deleteAllTournaments()
        insert(*newTournaments.toTypedArray())
    }

    /**Finds and updates [TournamentEntity] by primary key if it exists in `tournaments` table.
     *
     * Returns number of changed rows (0 or 1)
     */
    @Update
    fun update(tournamentEntity: TournamentEntity): Int

    /**Update tournament record with provided [url] only if it exists in database. Used with network updates
     * to solve problem with potential reordering of operations DELETE and UPDATE*/
    @Transaction
    fun updateIfExists(url: String, tournament: Tournament) {
        getTournament(url)?.let {
            //Find correct primary key [id] value by secondary key [url]
            tournament.primaryKey = it.primaryKey
            if (update(tournament.entity) > 0) {
                replaceMatches(tournament.primaryKey, tournament.matches)
            }
        }
    }

    @Delete
    fun delete(tournamentEntity: TournamentEntity)

    /**Delete tournament. Will delete associated matches as well due to ON_DELETE_CASCADE constraint.*/
    @Transaction
    fun delete(tournament: Tournament) {
        delete(tournament.entity)
    }

    /**Delete all tournaments and matches.*/
    @Query("DELETE FROM tournament")
    fun deleteAllTournaments()


    @Query("DELETE FROM `Match` where tournament_id = :foreignKey")
    fun deleteMatches(foreignKey: Int)
}