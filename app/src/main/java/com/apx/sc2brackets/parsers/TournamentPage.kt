package com.apx.sc2brackets.parsers

import com.apx.sc2brackets.models.Match
import com.apx.sc2brackets.models.MatchMap
import com.apx.sc2brackets.models.Player
import com.apx.sc2brackets.models.Player.Race
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.lang.IllegalArgumentException
import java.util.*

class TournamentPage private constructor(override val document: Document) : ParsedDocument {

    private fun getMapResult(mapBlock: Element): MatchMap.Result {
        val iconBlock = mapBlock.select("div img")
        return if (iconBlock.isEmpty()) {
            MatchMap.Result.NONE
        } else when (iconBlock[0].parent().elementSiblingIndex()) {
            0 -> MatchMap.Result.FIRST
            1 -> MatchMap.Result.SECOND
            else -> MatchMap.Result.NONE
        }
    }

    fun getMatch(matchBlock: Element, matchCategory: String): Match {
        //query player names
        val names = matchBlock.selectNotEmpty(Selectors.matchPlayers).map {
            it.select("img+span").text().ifEmpty { "TBD" }
        }
        val matchPoints = matchBlock.selectNotEmpty(Selectors.matchPoints)
            .eachText().map { it.toInt() }
            .ifEmpty { listOf(0, 0) }

        val races = matchBlock.select(
            "${Selectors.firstPlayerRace}, ${Selectors.secondPlayerRace}"
        )
            .eachAttr("title")
            .map { pickRace(it) }
            .ifEmpty { listOf(Race.TBD, Race.TBD) }

        val match = Match(
            firstPlayer = Player(names[0], races[0]),
            secondPlayer = Player(names[1], races[1]),
            category = matchCategory
        )
        match.score = Pair(matchPoints[0], matchPoints[1])
        matchBlock.selectFirst(Selectors.matchPopup)?.let {
            match.startTime = getMatchTime(it)
            match.isFinished = isFinished(it)
        }
        match.maps = getMatchMaps(matchBlock)
        return match
    }

    fun getMatchList(): List<Match> {
        val bracket = document.selectNotEmpty(Selectors.bracketBlock)[0]
        return bracket.select(Selectors.bracketColumn).flatMap<Element, Match> {
            val category = it.selectNotEmpty(Selectors.bracketHeader).text()
            it.select(Selectors.matchBlock)
                .map { matchBlock -> getMatch(matchBlock, category) }
        }
    }

    fun getMatchMaps(matchBlock: Element): List<MatchMap> {
        val bannedMaps = matchBlock.select(Selectors.matchMapBans)
        val playedMaps = matchBlock.select(Selectors.matchMaps) - bannedMaps
        return playedMaps.map {
            MatchMap(

                name = it.select(Selectors.matchMapName).ifEmpty { it.select("a") }.text(),
                winner = getMapResult(mapBlock = it)
            )
        } + bannedMaps.map {
            MatchMap(
                name = it.selectNotEmpty(Selectors.matchMapName).text(),
                crossedBy = getMapResult(mapBlock = it)
            )
        }
    }

    private fun getMatchTime(popupBlock: Element): DateTime? {
        val timeBlock = popupBlock.selectFirst(Selectors.matchTime) ?: return null
        //read descriptor and remove block to avoid text value contamination
        val timeZoneDescriptor = timeBlock.selectNotEmpty("abbr").run {
            val descriptor = attr("data-tz")
            remove()
            //cast to +/-hh:mm format with leading zero for joda-time parsing
            descriptor.run {
                if (length >= 6) {
                    this
                } else {
                    this[0] + "0" + substring(1)
                }
            }
        }
        //withZoneRetainFields preserves parsed time instead of converting it for new zone
        return timeParser.parseDateTime(timeBlock.text()).withZoneRetainFields(DateTimeZone.forID(timeZoneDescriptor))
    }

    fun getName() = document.selectNotEmpty(Selectors.tournamentName).text()

    private fun isFinished(popupBlock: Element): Boolean{
        val timeBlock = popupBlock.selectFirst(Selectors.matchTime) ?: return true
        return timeBlock.attr("data-finished").isNotEmpty()
    }

    @Throws(IllegalArgumentException::class)
    fun pickRace(raceName: String): Race {
        return Race.valueOf(raceName.toUpperCase().trim())
    }

    companion object : Parser<TournamentPage>() {
        override fun getInstance(doc: Document) = TournamentPage(doc)

        private object Selectors {
            const val bracketBlock = "div.bracket-wrapper.bracket-player"
            const val bracketColumn = "div.bracket-column.bracket-column-matches"
            const val bracketHeader = "div.bracket-header"

            const val matchBlock = "div.bracket-game"
            const val matchPlayers = "div.bracket-player-top, div.bracket-player-bottom"
            const val matchPoints = "div.bracket-score"
            const val matchPopup = "div.bracket-popup"

            const val firstPlayerRace = "div.bracket-popup-header-left a"
            const val secondPlayerRace = "div.bracket-popup-header-right a"

            const val matchTime = "span.timer-object"
            const val matchMaps = "div.bracket-popup-body-match"
            const val matchMapName = "a.mw-redirect"
            const val matchMapBans = "div.bracket-popup-body-bans ~ div.bracket-popup-body-match"

            const val tournamentName = "h1#firstHeading"
        }

        private val timeParser = DateTimeFormat.forPattern("MMMM d, YYYY - HH:mm").withLocale(Locale.ENGLISH)!!
    }
}