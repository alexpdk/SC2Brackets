package com.apx.sc2brackets.brackets

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.widget.RecyclerView
import android.util.Log

private const val TAG = "BracketPagerAdapter"

class BracketFragmentPagerAdapter(
    private val viewPool: RecyclerView.RecycledViewPool,
    fm: FragmentManager
) : FragmentPagerAdapter(fm) {

    private val tabs = arrayOf(MatchBracket.TimeFilter.TODAY, MatchBracket.TimeFilter.NEXT, MatchBracket.TimeFilter.PAST)

    override fun getItem(p0: Int): Fragment {
        Log.i(TAG, "getItem($p0) called")
        val fragment = BracketFragment.newInstance(timeFilter = tabs[p0])
        fragment.sharedRecycledViewPool = viewPool
        return fragment
    }

    override fun getCount() = tabs.size

    override fun getPageTitle(position: Int): CharSequence? {
        return tabs[position].name
    }
}