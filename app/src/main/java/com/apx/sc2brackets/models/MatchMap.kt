package com.apx.sc2brackets.models

import androidx.room.*
import com.apx.sc2brackets.db.ResultTypeConverter

@Entity(
    tableName = "maps",
    foreignKeys = [ForeignKey(
        entity = MatchEntity::class,
        parentColumns = ["id"],
        childColumns = ["match_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("match_id")]
)
@TypeConverters(ResultTypeConverter::class)
data class MatchMap(
    val name: String,
    var crossedBy: Result = Result.NONE,
    var winner: Result = Result.NONE
) {
    @PrimaryKey(autoGenerate = true)
    var id = 0

    @ColumnInfo(name = "match_id")
    var matchID = 0

    enum class Result {
        FIRST, SECOND, NONE;

        fun isFirst() = this == FIRST
        fun isSecond() = this == SECOND
    }

    companion object {
        val DEFAULT_MAPS = listOf(
            MatchMap(
                name = "Cyber Forest",
                winner = Result.FIRST
            ),
            MatchMap(
                name = "New Repugnancy",
                winner = Result.SECOND
            ),
            MatchMap(
                name = "Port Aleksander",
                winner = Result.FIRST
            )
        )
    }
}