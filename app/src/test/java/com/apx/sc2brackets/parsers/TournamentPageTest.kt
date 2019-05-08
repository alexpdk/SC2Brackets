package com.apx.sc2brackets.parsers

import com.apx.sc2brackets.models.Match
import com.apx.sc2brackets.models.MatchMap
import com.apx.sc2brackets.models.Player
import org.junit.jupiter.api.Test
import java.io.File
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.CoreMatchers.equalTo
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.jupiter.api.Assertions.assertTrue

import com.apx.sc2brackets.models.Player.Race.*
import com.apx.sc2brackets.models.MatchMap.Result.*
import org.hamcrest.CoreMatchers.nullValue

class TournamentPageTest {

    private val matches = object {
        val Hero_Trap by lazy {
            TournamentPage.parseFile(
                File(this.javaClass.getResource("/IEM_Katowice_hero_trap.html")!!.toURI()),
                "https://liquipedia.net/starcraft2/IEM_Season_XIII_-_Katowice"
            )
        }
        val Stats_Dark by lazy {
            TournamentPage.parseFile(
                File(this.javaClass.getResource("/IEM_Katowice_stats_dark.html")!!.toURI()),
                "https://liquipedia.net/starcraft2/IEM_Season_XIII_-_Katowice"
            )
        }
        val Serral_TBD by lazy {
            TournamentPage.parseFile(
                File(this.javaClass.getResource("/WCS_Spring_Qualifiers_Serral_TBD.html")!!.toURI()),
                ""
            )
        }
        val Bly_Enigma by lazy {
            TournamentPage.parseFile(
                File(this.javaClass.getResource("/Red_Hot_Cup1_Bly_Enigma.html")!!.toURI()),
                ""
            )
        }
        val IEM_Katowice by lazy {
            TournamentPage.parseFile(
                File(this.javaClass.getResource("/IEM_Katowice.html")!!.toURI()),
                "https://liquipedia.net/starcraft2/IEM_Season_XIII_-_Katowice"
            )
        }
        val WCS_Spring_Qualifiers by lazy {
            TournamentPage.parseFile(
                File(this.javaClass.getResource("/WCS_Spring_Qualifiers(Europe).html")!!.toURI()),
                ""
            )
        }
    }

    @Test
    fun `Test HERO_TRAP match parsing`() {
        val match = matches.Hero_Trap.getMatch(matches.Hero_Trap.document, "Round of 12")
        assertThat(
            match, equalTo(
                Match(
                    Player("herO", PROTOSS),
                    Player("Trap", PROTOSS),
                    "Round of 12"
                )
            )
        )
        assertThat(match.score, equalTo(Pair(3, 0)))
        match.startTime?.let {
            assertTrue(
                it.isEqual(
                    DateTime(
                        2019, 3, 2, 12, 0, 0,
                        DateTimeZone.forID("CET")
                    )
                )
            )
        }
        val maps = matches.Hero_Trap.getMatchMaps(matches.Hero_Trap.document)
        assertThat(
            maps, equalTo(
                listOf(
                    MatchMap("Cyber Forest", winner = FIRST),
                    MatchMap("Port Aleksander", winner = FIRST),
                    MatchMap("New Repugnancy", winner = FIRST)
                )
            )
        )
    }

    @Test
    fun `Test STATS_DARK match parsing`() {
        val match = matches.Stats_Dark.getMatch(matches.Stats_Dark.document, "Semifinals")
        assertThat(
            match, equalTo(
                Match(
                    Player("Stats", PROTOSS),
                    Player("Dark", ZERG),
                    "Semifinals"
                )
            )
        )
        assertThat(match.score, equalTo(Pair(3, 1)))
        match.startTime?.let {
            assertTrue(
                it.isEqual(
                    DateTime(
                        2019, 3, 3, 12, 15, 0,
                        DateTimeZone.forID("CET")
                    )
                )
            )
        }
        val maps = matches.Stats_Dark.getMatchMaps(matches.Stats_Dark.document)
        assertThat(
            maps, equalTo(
                listOf(
                    MatchMap("Automaton", winner = SECOND),
                    MatchMap("New Repugnancy", winner = FIRST),
                    MatchMap("Port Aleksander", winner = FIRST),
                    MatchMap("Year Zero", winner = FIRST),
                    MatchMap("Kairos Junction", winner = NONE),
                    MatchMap("King's Cove", crossedBy = FIRST),
                    MatchMap("Cyber Forest", crossedBy = SECOND)
                )
            )
        )
    }

    @Test
    fun `Test SERRAL_TBD match parsing`() {
        val match = matches.Serral_TBD.getMatch(matches.Serral_TBD.document, "Quarterfinals")
        assertThat(
            match, equalTo(
                Match("Serral", ZERG, "TBD", TBD, "Quarterfinals")
            )
        )
        assertThat(match.score, equalTo(Pair(0, 0)))
        match.startTime?.let {
            assertTrue(
                it.isEqual(
                    DateTime(
                        2019, 5, 4, 14, 0, 0,
                        DateTimeZone.forID("+02:00")
                    )
                )
            )
        }
    }

    @Test
    fun `Test Bly_Enigma match parsing`() {
        val match = matches.Bly_Enigma.getMatch(matches.Bly_Enigma.document, "Quarterfinals")
        assertThat(
            match, equalTo(
                Match("Bly", TBD, "EnigmA", TBD, "Quarterfinals")
            )
        )
        assertThat(match.score, equalTo(Pair(2, 0)))
        assertThat(match.startTime, nullValue())

        val maps = matches.Bly_Enigma.getMatchMaps(matches.Bly_Enigma.document)
        assertThat(maps, equalTo(emptyList()))
    }

    @Test
    fun `Test IEM_Katowice bracket parsing`() {
        val matches = matches.IEM_Katowice.getFullBracket()
        assertThat(
            matches, equalTo(
                listOf(
                    Match("herO", PROTOSS, "Trap", PROTOSS, "Round of 12 (Bo5)"),
                    Match("Zest", PROTOSS, "soO", ZERG, "Round of 12 (Bo5)"),
                    Match("Neeb", PROTOSS, "RagnaroK", ZERG, "Round of 12 (Bo5)"),
                    Match("TY", TERRAN, "Solar", ZERG, "Round of 12 (Bo5)"),
                    Match("Dear", PROTOSS, "herO", PROTOSS, "Quarterfinals (Bo5)"),
                    Match("Serral", ZERG, "soO", ZERG, "Quarterfinals (Bo5)"),
                    Match("Stats", PROTOSS, "Neeb", PROTOSS, "Quarterfinals (Bo5)"),
                    Match("Dark", ZERG, "Solar", ZERG, "Quarterfinals (Bo5)"),
                    Match("herO", PROTOSS, "soO", ZERG, "Semifinals (Bo5)"),
                    Match("Stats", PROTOSS, "Dark", ZERG, "Semifinals (Bo5)"),
                    Match("soO", ZERG, "Stats", PROTOSS, "Finals (Bo7)")
                )
            )
        )
    }

    @Test
    fun `Test WCS Spring Qualifiers bracket parsing`() {
        val matches = matches.WCS_Spring_Qualifiers.getFullBracket()
        assertThat(
            matches, equalTo(
                listOf(
                    Match("Serral", ZERG, "TBD", TBD, "Quarterfinals (Bo5)"),
                    Match("TBD", TBD, "TBD", TBD, "Quarterfinals (Bo5)"),
                    Match("TBD", TBD, "TBD", TBD, "Quarterfinals (Bo5)"),
                    Match("TBD", TBD, "TBD", TBD, "Quarterfinals (Bo5)"),
                    Match("TBD", TBD, "TBD", TBD, "Semifinals (Bo5)"),
                    Match("TBD", TBD, "TBD", TBD, "Semifinals (Bo5)"),
                    Match("TBD", TBD, "TBD", TBD, "Finals (Bo7)")
                )
            )
        )
    }
}