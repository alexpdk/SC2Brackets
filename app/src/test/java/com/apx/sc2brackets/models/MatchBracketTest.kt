package com.apx.sc2brackets.models

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class MatchBracketTest {
    private val cases = mapOf(
        "basic" to Case(
            matches = listOf(
                Match(MatchEntity("Player1", "Player2", "Round1")),
                Match(MatchEntity("Player1", "Player2", "Round2"))
            ), bracket = listOf(
                MatchBracket.Header("Round1"),
                Match(MatchEntity("Player1", "Player2", "Round1")),
                MatchBracket.Header("Round2"),
                Match(MatchEntity("Player1", "Player2", "Round2")),
                MatchBracket.Header(MatchBracket.BRACKET_END_TEXT)
            )
        ),
        "complex" to Case(
            matches = listOf(
                Match(MatchEntity("Player1", "Player2", "Group A")),
                Match(MatchEntity("Player3", "Player4", "Group B")),
                Match(MatchEntity("Player5", "Player6", "Group B")),
                Match(MatchEntity("Player1", "Player3", "3rd place match")),
                Match(MatchEntity("Player2", "Player4", "Final"))
            ), bracket = listOf(
                MatchBracket.Header("Group A"),
                Match(MatchEntity("Player1", "Player2", "Group A")),
                MatchBracket.Header("Group B"),
                Match(MatchEntity("Player3", "Player4", "Group B")),
                Match(MatchEntity("Player5", "Player6", "Group B")),
                MatchBracket.Header("3rd place match"),
                Match(MatchEntity("Player1", "Player3", "3rd place match")),
                MatchBracket.Header("Final"),
                Match(MatchEntity("Player2", "Player4", "Final")),
                MatchBracket.Header(MatchBracket.BRACKET_END_TEXT)
            )
        )
    )

    @Test
    fun `Test bracket creation`() {
        for (case in cases.values) {
            val bracket = MatchBracket(case.matches)
            assertTrue(bracket.hasItemList(case.bracket))
        }
    }

    @Test
    fun `Test match removal`() {
        val bracket = MatchBracket(cases.getValue("complex").matches)
        val groups = mutableMapOf(
            "GroupA" to listOf(
                MatchBracket.Header("Group A"),
                Match(
                    MatchEntity("Player1", "Player2", "Group A")
                )
            ),
            "GroupB" to listOf(
                MatchBracket.Header("Group B"),
                Match(
                    MatchEntity("Player3", "Player4", "Group B")
                ),
                Match(
                    MatchEntity("Player5", "Player6", "Group B")
                )
            ),
            "3rd place match" to listOf(
                MatchBracket.Header("3rd place match"),
                Match(
                    MatchEntity("Player1", "Player3", "3rd place match")
                )
            ),
            "Final" to listOf(
                MatchBracket.Header("Final"),
                Match(MatchEntity("Player2", "Player4", "Final"))
            ),
            "Footer" to listOf(
                MatchBracket.Header(MatchBracket.BRACKET_END_TEXT)
            )
        )
        assertTrue(bracket.hasItemList(groups.values.flatten()))

        bracket.remove(MatchEntity("Player1", "Player3", "3rd place match"))
        groups.remove("3rd place match")
        assertTrue(bracket.hasItemList(groups.values.flatten()), "Match removal removes 1-match group as well")

        bracket.remove(MatchEntity("Player5", "Player6", "Group B"))
        groups.replace(
            "GroupB", listOf(
                MatchBracket.Header("Group B"),
                Match(MatchEntity("Player3", "Player4", "Group B"))
            )
        )
        assertTrue(bracket.hasItemList(groups.values.flatten()), "Indices correctly changed")

        bracket.remove(MatchEntity("Player2", "Player4", "Final"))
        groups.remove("Final")
        assertTrue(bracket.hasItemList(groups.values.flatten()))
    }

    data class Case(val matches: List<Match>, val bracket: List<MatchBracket.BracketItem>)
}