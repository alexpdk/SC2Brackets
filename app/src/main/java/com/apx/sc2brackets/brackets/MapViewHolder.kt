package com.apx.sc2brackets.brackets

import android.annotation.SuppressLint
import android.view.Gravity
import android.view.View
import com.apx.sc2brackets.maps.MatchMap

import kotlinx.android.synthetic.main.map_result.view.*

private const val TAG = "MapViewHolder"

class MapViewHolder(val view: View, private val adapter: IBracketAdapter) {

    @SuppressLint("RtlHardcoded")
    fun display(map: MatchMap, match: Match) {
        with(view.map_name){
            text = map.name
            setTextColor(getColor(match, map.winner))
            if(map.winner.isSecond()){
                gravity = Gravity.RIGHT
            }
        }
        view.map_stats.text = "TvP 34.17%"
        view.first_player_mark.setImageDrawable(
            getLogo(match, map.winner, isWinner = map.winner.isFirst())
        )
        view.second_player_mark.setImageDrawable(
            getLogo(match, map.winner, isWinner = map.winner.isSecond())
        )
    }

    private fun getLogo(match: Match, result: MatchMap.Result, isWinner: Boolean) = if (isWinner) {
        when (result) {
            MatchMap.Result.FIRST -> adapter.loader.getWinLogo(match.races.first)
            MatchMap.Result.SECOND -> adapter.loader.getWinLogo(match.races.second)
            MatchMap.Result.NONE -> adapter.loader.getSpaceLogo()
        }
    } else {
        adapter.loader.getSpaceLogo()
    }

    private fun getColor(match: Match, result: MatchMap.Result) = when (result) {
        MatchMap.Result.FIRST -> adapter.loader.getWinColor(match.races.first)
        MatchMap.Result.SECOND -> adapter.loader.getWinColor(match.races.second)
        MatchMap.Result.NONE -> adapter.loader.getColor(android.R.color.black)
    }
}