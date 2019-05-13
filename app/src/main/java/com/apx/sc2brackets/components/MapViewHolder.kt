package com.apx.sc2brackets.components

import android.annotation.SuppressLint
import android.view.Gravity
import android.view.View
import com.apx.sc2brackets.MyResourceLoader
import com.apx.sc2brackets.models.MatchMap
import com.apx.sc2brackets.models.Match

import kotlinx.android.synthetic.main.map_result.view.*

private const val TAG = "MapViewHolder"

class MapViewHolder(val view: View, private val resourceLoader: MyResourceLoader) {

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
            MatchMap.Result.FIRST -> resourceLoader.getRaceWinLogo(match.firstPlayer.race)
            MatchMap.Result.SECOND -> resourceLoader.getRaceWinLogo(match.secondPlayer.race)
            MatchMap.Result.NONE -> resourceLoader.getBlankLogo()
        }
    } else {
        resourceLoader.getBlankLogo()
    }

    private fun getColor(match: Match, result: MatchMap.Result) = when (result) {
        MatchMap.Result.FIRST -> resourceLoader.getRaceColor(match.firstPlayer.race)
        MatchMap.Result.SECOND -> resourceLoader.getRaceColor(match.secondPlayer.race)
        MatchMap.Result.NONE -> resourceLoader.getColor(android.R.color.black)
    }
}