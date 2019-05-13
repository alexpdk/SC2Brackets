package com.apx.sc2brackets.adapters

import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.apx.sc2brackets.components.BracketFragment
import com.apx.sc2brackets.models.MatchBracket

class BracketFragmentPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    override fun getItem(p0: Int) = BracketFragment.newInstance(timeFilter = tabs[p0])
    override fun getCount() = tabs.size
    override fun getPageTitle(position: Int) = tabs[position].name

    companion object {
        private val tabs = arrayOf(MatchBracket.TimeFilter.TODAY, MatchBracket.TimeFilter.NEXT, MatchBracket.TimeFilter.PAST)

        val TODAY_TAB_INDEX = tabs.indexOf(MatchBracket.TimeFilter.TODAY)
        val FUTURE_TAB_INDEX = tabs.indexOf(MatchBracket.TimeFilter.NEXT)
        val PAST_TAB_INDEX = tabs.indexOf(MatchBracket.TimeFilter.PAST)
    }
}