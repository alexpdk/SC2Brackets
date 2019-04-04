package com.apx.sc2brackets.brackets

class MatchBracket(list: List<Match>) {

    private var matches = list.toMutableList()
    private var listWithHeaders = addHeaders(matches)

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

    operator fun get(i: Int): BracketItem {
        return listWithHeaders[i]
    }

    val list: List<BracketItem> get() = listWithHeaders

    fun remove(match: Match) {
        matches.remove(match)
        listWithHeaders = addHeaders(matches)
    }

    interface BracketItem
    data class Header(val content: String) : BracketItem

    companion object {
        /**Bracket that contains "Round of 2", "Round of 4", "Round of 8" and "Round of 12" matches*/
        val DEFAULT_TOURNAMENT = MatchBracket(
            listOf(2, 4, 8, 12).map { i->
                Array(i/2){Match.random("Round of $i")}.toList()
            }.flatten()
        )
    }
}