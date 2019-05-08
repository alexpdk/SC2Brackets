package com.apx.sc2brackets.models

import org.joda.time.DateTime
import java.lang.StringBuilder

class MatchBracket(list: List<Match>, val isLoading: Boolean = false) {

    private var matches = list.toMutableList()
    private var listWithHeaders = addFooter(addHeaders(matches))

    /*
     * Logic is currently approximate and implemented for the sake of layout demonstration
     */
    //TODO: use some implementation of InvalidatableLazy
    private var nextList = getNextList()
    private var pastList = getPastList()
    private var todayList = getTodayList()

    private fun addFooter(list: List<BracketItem>): List<BracketItem>{
        val footer = Header(
            when{
                isLoading -> LOADING_MATCHES_TEXT
                list.isEmpty() -> NO_MATCHES_TEXT
                list.last() == matches.last() -> LAST_FOOTER_TEXT
                else -> FOOTER_TEXT
            }
        )
        return list.toMutableList().apply {
            add(footer)
        }
    }

    private fun addHeaders(list: List<Match>): List<BracketItem> {
        // group matches by category to be displayed under common header
        val matchGroups = list.sortedBy { it.startTime }.groupBy { it.category }

        // create headers with same indices as first matches of corresponding group
        val headers = Array<Header?>(list.size) { null }
        var sizeAcc = 0
        for ((name, group) in matchGroups) {
            headers[sizeAcc] = Header(name)
            sizeAcc += group.size
        }
        // prepend matches with headers, drop empty headers
        return list.zip(headers)
            .flatMap { (match, header) -> header?.let { listOf(header, match) } ?: listOf(match) }
    }

    fun filter(filter: TimeFilter?) = when (filter) {
        null -> listWithHeaders
        TimeFilter.NEXT -> nextList
        TimeFilter.PAST -> pastList
        TimeFilter.TODAY -> todayList
    }

    operator fun get(i: Int): BracketItem {
        return listWithHeaders[i]
    }

    private fun getNextList(): List<BracketItem> {
        val nextDay = DateTime.now().plusDays(1)
        val list = matches.dropWhile { it.isBefore(nextDay) }
        return addFooter(addHeaders(list))
    }
    private fun getPastList(): List<BracketItem> {
        val now = DateTime.now()
        val list = matches.takeWhile { it.isBefore(now) }
        return addFooter(addHeaders(list))
    }
    private fun getTodayList(): List<BracketItem> {
        val now = DateTime.now()
        val nextDay = DateTime.now().plusDays(1)
        val list = matches.dropWhile { it.isBefore(now) }.takeWhile { it.isBefore(nextDay) }
        return addFooter(addHeaders(list))
    }

    fun isEmpty() = matches.isEmpty()

    val list: List<BracketItem> get() = listWithHeaders

    fun remove(match: Match) {
        matches.remove(match)
        listWithHeaders = addHeaders(matches)
        nextList = getNextList()
        pastList = getPastList()
        todayList = getTodayList()
    }

    override fun toString(): String {
        val b = StringBuilder("MatchBracket(")
        if(list.isNotEmpty()){
            b.append("Header = ${list[0]}")
        }
        if(matches.isNotEmpty()){
            b.append("\nFirst match = ${matches[0]}")
        }
        b.append(")")
        return b.toString()
    }

    interface BracketItem
    data class Header(val content: String) : BracketItem

    enum class TimeFilter {
        TODAY, NEXT, PAST
    }

    companion object {
        /**Bracket that contains "Round of 2", "Round of 4", "Round of 8" and "Round of 12" matches*/
        val DEFAULT_TOURNAMENT = MatchBracket(
            listOf(12, 8, 4, 2).map { i ->
                Array(i / 2) { Match.random("Round of $i") }.toList()
            }.flatten().apply {
                for (i in 0..3) this[i].startTime = DateTime().minusHours(1)
                for (i in 4..8) this[i].startTime = DateTime().plusHours(1)
                for (i in 9 until size) this[i].startTime = DateTime().plusDays(1).plusHours(1)
            }
        )
        const val FOOTER_TEXT = "Swipe to see more matches"
        const val LAST_FOOTER_TEXT = "End of the tournament"
        const val NO_MATCHES_TEXT = "No matches in this category"
        const val LOADING_MATCHES_TEXT = "Loading match data..."
        val LOADING_BRACKET_STUB = MatchBracket(emptyList(), isLoading = true)
    }
}