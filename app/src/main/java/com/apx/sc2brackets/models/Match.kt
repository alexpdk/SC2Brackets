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
    tableName = "Match",
    foreignKeys = [ForeignKey(
        entity = TournamentEntity::class,
        parentColumns = ["id"],
        childColumns = ["tournament_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("tournament_id")]
)
data class MatchEntity(
    @Embedded(prefix = "first_") var firstPlayer: Player,
    @Embedded(prefix = "second_") var secondPlayer: Player,
    var category: String
) {

    @ColumnInfo(name = "tournament_id")
    var tournamentID = 0

    @PrimaryKey(autoGenerate = true)
    var id = 0

    @TypeConverters(ScoreTypeConverter::class)
    var score = Pair(0, 0)

    // Null time means match is to be scheduled in the future
    @TypeConverters(DateTimeTypeConverter::class)
    var startTime: DateTime? = null
}

class Match(firstPlayer: Player = Player.TBD, secondPlayer: Player = Player.TBD, category: String = "") :
    MatchBracket.BracketItem {
    @Embedded
    var entity = MatchEntity(firstPlayer, secondPlayer, category)

    @Relation(entity = MatchMap::class, parentColumn = "id", entityColumn = "match_id")
            /**List of played matches sorted by start order.*/
    var maps: List<MatchMap> = emptyList()

    /**Are match details expanded inside list, solution for implementing expand/collapse animation.*/
    @Ignore
    var detailsExpanded = false

    override fun equals(other: Any?): Boolean {
        return entity == other
    }

    override fun hashCode(): Int {
        return entity.hashCode()
    }

    @Ignore
    var isFinished = true

    val firstPlayer get() = entity.firstPlayer
    /**Does match have any meaningful score (other than 0:0)*/
    private val hasScore get() = entity.score.first > 0 || entity.score.second > 0
    // Match with unknown time and score considered to be in the past, without score - in the future
    fun isBefore(moment: DateTime) = entity.startTime?.isBefore(moment) ?: hasScore
    /**Is match played right now*/
    val isLive get() = isBefore(DateTime.now()) && !isFinished

    var score: Pair<Int, Int>
        get() = entity.score
        set(value) {
            entity.score = value
        }
    val secondPlayer get() = entity.secondPlayer

    var startTime: DateTime?
        get() = entity.startTime
        set(value) {
            entity.startTime = value
        }

    var tournamentID: Int
        get() = entity.tournamentID
        set(value) {
            entity.tournamentID = value
        }

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