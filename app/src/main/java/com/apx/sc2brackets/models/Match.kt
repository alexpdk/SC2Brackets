package com.apx.sc2brackets.models

import androidx.room.*
import com.apx.sc2brackets.db.DateTimeTypeConverter
import com.apx.sc2brackets.db.ScoreTypeConverter
import org.joda.time.*
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
    var isFinished: Boolean = false

    @ColumnInfo(name = "tournament_id")
    var tournamentID = 0

    @PrimaryKey(autoGenerate = true)
    var id = 0

    @TypeConverters(ScoreTypeConverter::class)
    var score = Pair(0, 0)

    // Null time means match is to be scheduled in the future
    @TypeConverters(DateTimeTypeConverter::class)
    var startTime: DateTime? = null

    /*created and used only for testing purposes*/
    @TestOnly
    constructor(firstPlayerName: String, secondPlayerName: String, category: String) : this(
        firstPlayer = Player(firstPlayerName, TBD),
        secondPlayer = Player(secondPlayerName, TBD),
        category = category
    )

    /*created and used only for testing purposes*/
    @TestOnly
    constructor(
        firstPlayerName: String,
        firstRace: Player.Race,
        secondPlayerName: String,
        secondRace: Player.Race,
        category: String
    ) : this(
        firstPlayer = Player(firstPlayerName, firstRace),
        secondPlayer = Player(secondPlayerName, secondRace),
        category = category
    )
}

class Match(@Embedded var entity: MatchEntity) :
    MatchBracket.BracketItem {

    constructor(firstPlayer: Player = Player.TBD, secondPlayer: Player = Player.TBD, category: String = "") : this(
        MatchEntity(
            firstPlayer, secondPlayer, category
        )
    )

    @Relation(entity = MatchMap::class, parentColumn = "id", entityColumn = "match_id")
            /**List of played matches sorted by start order.*/
    var maps: List<MatchMap> = emptyList()

    /**Are match details expanded inside list, solution for implementing expand/collapse animation.*/
    @Ignore
    var detailsExpanded = false

    val firstPlayer get() = entity.firstPlayer

    private val hasScore get() = score.first != 0 || score.second != 0

    var isFinished: Boolean
        get() = entity.isFinished || (entity.startTime == null && hasScore)
        set(value) {
            entity.isFinished = value
        }

    /**Is match played right now: started before current moment, but not finished yet*/
    val isLive get() = startsBefore(DateTime.now()) && !isFinished

    var score: Pair<Int, Int>
        get() = entity.score
        set(value) {
            entity.score = value
        }
    val scoreString
        get() = when {
            score.first < 0 -> "-:W"
            score.second < 0 -> "W:-"
            else -> "${score.first}:${score.second}"
        }

    val secondPlayer get() = entity.secondPlayer

    fun startsAfter(moment: DateTime) = entity.startTime?.isAfter(moment) ?: !isFinished
    fun startsBefore(moment: DateTime) = entity.startTime?.isBefore(moment) ?: isFinished

    val startsInHour: Boolean
        get() {
            val now = DateTime.now()
            return startsAfter(now) && startsBefore(now.plusHours(1))
        }
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