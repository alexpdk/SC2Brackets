package com.apx.sc2brackets.models

import com.apx.sc2brackets.utils.dayEnd
import com.apx.sc2brackets.utils.dayStart
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

    private fun addFooter(list: List<BracketItem>): List<BracketItem> {
        val footer = Header(
            when {
                isLoading -> LOADING_MATCHES_TEXT
                list.isEmpty() -> NO_MATCHES_TEXT
                list.last() == matches.last() -> BRACKET_END_TEXT
                else -> PAGE_FOOTER_TEXT
            }
        )
        return list.toMutableList().apply {
            add(footer)
        }
    }

    private fun addHeaders(list: List<Match>): List<BracketItem> {
        // group matches by category to be displayed under common header
        val matchGroups = list.sortedBy { it.startTime }.groupBy { it.entity.category }

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
        val dayEnd = dayEnd(DateTime.now())
        val list = matches.dropWhile { it.startsBefore(dayEnd) }
        return addFooter(addHeaders(list))
    }

    private fun getPastList(): List<BracketItem> {
        val dayStart = dayStart(DateTime.now())
        val list = matches.takeWhile { it.startsBefore(dayStart) }
        return addFooter(addHeaders(list))
    }

    private fun getTodayList(): List<BracketItem> {
        val now = DateTime.now()
        val dayStart = dayStart(now)
        val dayEnd = dayEnd(now)
        val list = matches.dropWhile { it.startsBefore(dayStart) }.takeWhile { it.startsBefore(dayEnd) }
        return addFooter(addHeaders(list))
    }

    fun hasItemList(itemList: List<BracketItem>): Boolean {
        if (list.size != itemList.size) {
            return false
        }
        return itemList.zip(list).all {
            val first = it.first
            val second = it.second
            when (first) {
                is Match -> if (second is Match) {
                    first.entity == second.entity
                } else {
                    false
                }
                else -> first == second
            }
        }
    }

    fun isEmpty() = matches.isEmpty()

    val list: List<BracketItem> get() = listWithHeaders

    fun remove(matchEntity: MatchEntity) {
        with(matches) {
            remove(find { it.entity == matchEntity })
        }
        listWithHeaders = addFooter(addHeaders(matches))
        nextList = getNextList()
        pastList = getPastList()
        todayList = getTodayList()
    }

    override fun toString(): String {
        val b = StringBuilder("MatchBracket(")
        if (list.isNotEmpty()) {
            b.append("Header = ${list[0]}")
        }
        if (matches.isNotEmpty()) {
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
        const val PAGE_FOOTER_TEXT = "Swipe to see more matches"
        const val BRACKET_END_TEXT = "End of the tournament"
        const val NO_MATCHES_TEXT = "No matches in this category"
        const val LOADING_MATCHES_TEXT = "Loading match data..."

        /**Special list to substitute bracket while the real data is downloaded.
         * Provides bracket list with [LOADING_MATCHES_TEXT] headers.*/
        val LOADING_BRACKET_STUB = MatchBracket(emptyList(), isLoading = true)
        /**Empty bracket when load is complete indicates that error happened and data
         * cannot be obtained.*/
        val ERROR_BRACKET_STUB = MatchBracket(emptyList())
    }
}