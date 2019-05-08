package com.apx.sc2brackets.brackets

import android.annotation.SuppressLint
import android.view.Gravity
import android.view.View
import com.apx.sc2brackets.models.MatchMap
import com.apx.sc2brackets.models.Match

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
        view.first_player_mark.setImageDrawable(
            getLogo(match, map.winner, isWinner = map.winner.isFirst())
        )
        view.second_player_mark.setImageDrawable(
            getLogo(match, map.winner, isWinner = map.winner.isSecond())
        )
    }

    private fun getLogo(match: Match, result: MatchMap.Result, isWinner: Boolean) = if (isWinner) {
        when (result) {
            MatchMap.Result.FIRST -> adapter.loader.getRaceWinLogo(match.firstPlayer.race)
            MatchMap.Result.SECOND -> adapter.loader.getRaceWinLogo(match.secondPlayer.race)
            MatchMap.Result.NONE -> adapter.loader.getBlankLogo()
        }
    } else {
        adapter.loader.getBlankLogo()
    }

    private fun getColor(match: Match, result: MatchMap.Result) = when (result) {
        MatchMap.Result.FIRST -> adapter.loader.getRaceColor(match.firstPlayer.race)
        MatchMap.Result.SECOND -> adapter.loader.getRaceColor(match.secondPlayer.race)
        MatchMap.Result.NONE -> adapter.loader.getColor(android.R.color.black)
    }
}