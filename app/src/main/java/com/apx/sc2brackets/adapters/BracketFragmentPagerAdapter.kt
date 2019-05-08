package com.apx.sc2brackets.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.apx.sc2brackets.brackets.BracketFragment
import com.apx.sc2brackets.models.MatchBracket

class BracketFragmentPagerAdapter(
    private val viewPool: androidx.recyclerview.widget.RecyclerView.RecycledViewPool,
    fm: FragmentManager
) : FragmentPagerAdapter(fm) {

    override fun getItem(p0: Int): Fragment {
        val fragment =
            BracketFragment.newInstance(timeFilter = tabs[p0])
        fragment.sharedRecycledViewPool = viewPool
        return fragment
    }

    override fun getCount() = tabs.size

    override fun getPageTitle(position: Int): CharSequence? {
        return tabs[position].name
    }

    companion object {
        private val tabs = arrayOf(MatchBracket.TimeFilter.TODAY, MatchBracket.TimeFilter.NEXT, MatchBracket.TimeFilter.PAST)

        val TODAY_TAB_INDEX = tabs.indexOf(MatchBracket.TimeFilter.TODAY)
        val FUTURE_TAB_INDEX = tabs.indexOf(MatchBracket.TimeFilter.NEXT)
        val PAST_TAB_INDEX = tabs.indexOf(MatchBracket.TimeFilter.PAST)
    }
}