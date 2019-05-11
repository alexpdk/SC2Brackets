package com.apx.sc2brackets.models

import androidx.room.*
import com.apx.sc2brackets.db.DateTimeTypeConverter
import com.apx.sc2brackets.db.ScoreTypeConverter
import org.joda.time.*
import java.util.*
import com.apx.sc2brackets.models.Player.Race.*
import org.jetbrains.annotations.TestOnly

// Same issue with `val` as in [Player]
@Entity(
    foreignKeys = [ForeignKey(
        entity = TournamentEntity::class,
        parentColumns = ["id"],
        childColumns = ["tournament_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("tournament_id")]
)
data class Match(
    @Embedded(prefix = "first_") var firstPlayer: Player,
    @Embedded(prefix = "second_") var secondPlayer: Player,
    var category: String
) : MatchBracket.BracketItem {

    @Ignore
    var isFinished = true

    @ColumnInfo(name = "tournament_id")
    var tournamentID = 0

    @PrimaryKey
    var uuid = UUID.randomUUID().toString()

    @TypeConverters(ScoreTypeConverter::class)
    var score = Pair(0, 0)

    // Null time means match is to be scheduled in the future
    @TypeConverters(DateTimeTypeConverter::class)
    var startTime: DateTime? = null

    /**Are match details expanded inside list. Temporary solution for implementing expand/collapse animation.
     * Better to implement in ViewModel, like in TournamentsRecyclerViewAdapter*/
    @Ignore
    var detailsExpanded = false

    @Ignore
    var maps = MatchMap.DEFAULT_MAPS

    /*created and used only for testing purposes*/
    @TestOnly
    constructor(firstPlayer: String, secondPlayer: String, category: String) : this(
        Player(firstPlayer, TBD), Player(secondPlayer, TBD), category
    )

    /*created and used only for testing purposes*/
    @TestOnly
    constructor(
        firstPlayer: String,
        firstRace: Player.Race,
        secondPlayer: String,
        secondRace: Player.Race,
        category: String
    ) : this(
        Player(firstPlayer, firstRace), Player(secondPlayer, secondRace), category
    )

    val hasScore get() = score.first > 0 || score.second > 0
    // Match with unknown time and score considered to be in the past, without score - in the future
    fun isBefore(moment: DateTime) = startTime?.isBefore(moment) ?: hasScore

    companion object {
        private val PLAYERS = listOf(
            Player("Maru", TERRAN),
            Player("Serral", ZERG),
            Player("SpeCial", TERRAN),
            Player("Trap", PROTOSS),
            Player("PtitDrogo", TERRAN),
            Player("KingCobra", PROTOSS),
            Player("Kelazhur", TERRAN),
            Player("Scarlett", ZERG),
            Player("Neeb", PROTOSS)
        )

        fun random(category: String = "Custom games"): Match {
            val pair = PLAYERS.shuffled().take(2)
            return Match(pair[0], pair[1], category)
        }
    }
}