package com.apx.sc2brackets.components

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isEmpty
import com.apx.sc2brackets.R
import com.apx.sc2brackets.activities.BracketActivity
import com.apx.sc2brackets.adapters.BracketRecyclerViewAdapter
import com.apx.sc2brackets.utils.countDown
import com.apx.sc2brackets.models.Match
import com.apx.sc2brackets.models.MatchMap
import com.apx.sc2brackets.utils.stringify

import kotlinx.android.synthetic.main.match_details.view.*
import kotlinx.android.synthetic.main.match_item.view.*
import java.lang.StringBuilder

private const val TAG = "MatchViewHolder"

/**[MatchViewHolder] represents reusable ViewHolder inside the pool, shared by several [BracketFragment]
 * instances. It is not implemented as inner non-static class to avoid issues with referencing one [BracketFragment]
 * from another one.
 *
 * However, [MatchViewHolder] references [com.apx.sc2brackets.adapters.BracketRecyclerViewAdapter] object.
 * Reference is established via [setAdapter] method ans should be nullified as soon as ViewHolder is released after usage
 * in fragment with [clearAdapter].*/
class MatchViewHolder(val view: View, private val context: Context) :
    androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {

    private var bracketAdapter: BracketRecyclerViewAdapter? = null
    private var oldAdapterPosition = -1

    private val onRaceButtonClick = View.OnClickListener { button: View ->
        val match = view.tag as Match
        val player = if (button == view.first_player_button) {
            match.firstPlayer
        } else {
            match.secondPlayer
        }
        (context as BracketActivity).lookupPlayer(player)
    }

    private val onViewClick = View.OnClickListener { v: View ->
        bracketAdapter?.apply {
            val item = v.tag as Match
            item.detailsExpanded = !item.detailsExpanded
            //TODO: study and use payload parameter for efficient rebind
            notifyItemChanged(adapterPosition)
        }
    }

    init {
        view.setOnClickListener(onViewClick)
        view.first_player_button.setOnClickListener(onRaceButtonClick)
        view.second_player_button.setOnClickListener(onRaceButtonClick)
    }

    fun onBind(match: Match, position: Int) {
        match.detailsExpanded = isExpanded(position)
        //allows backward navigation to match as item list is interacted with
        view.tag = match

        view.setBackgroundColor(
            ContextCompat.getColor(
                context,
                arrayOf(R.color.terranBackground, R.color.protossBackground, R.color.zergBackground)[position % 3]
            )
        )

        //TODO: fix that binding cache is not used
        //Detailed info: https://antonioleiva.com/kotlin-android-extensions/
        view.first_player_name.text = match.firstPlayer.name
        view.second_player_name.text = match.secondPlayer.name
        view.match_score.text = match.score.run { "$first:$second" }
        val colorID = if (match.isLive) {
            R.color.liveGreen
        } else {
            android.R.color.black
        }
        view.match_score.setTextColor(ContextCompat.getColor(context, colorID))

        val resourceLoader = (context as BracketActivity).resourceLoader
        view.first_player_button.setImageDrawable(
            resourceLoader.getRaceLogo(match.firstPlayer.race)
        )
        view.second_player_button.setImageDrawable(
            resourceLoader.getRaceLogo(match.secondPlayer.race)
        )
        view.match_details_frame.visibility = if (match.detailsExpanded) {
            showDetails(match)
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    private fun isExpanded(newPosition: Int): Boolean {
        val wasExpanded = (view.tag as? Match)?.detailsExpanded ?: false
        val result = wasExpanded && (newPosition == oldAdapterPosition)
        oldAdapterPosition = newPosition
        return result
    }

    private fun printMatchTime(match: Match): String {
        val s = StringBuilder()
        s.append(stringify(match.startTime))
        s.append('\n')
        if (match.isFinished) {
            s.append(countDown(match.startTime))
        } else {
            s.append("Live (started ${countDown(match.startTime)})")
        }
        return s.toString()
    }

    /**Each time ViewHolder is bound and displayed, it should obtain reference to the adapter*/
    fun setAdapter(adapter: BracketRecyclerViewAdapter) {
        if(bracketAdapter != adapter){
            (view.tag as? Match)?.detailsExpanded = false
        }
        bracketAdapter = adapter
    }

    private fun showDetails(match: Match) {
        val inflater = LayoutInflater.from(context)
        val linearLayout = (if (view.match_details_frame.isEmpty()) {
            val layout = inflater.run {
                inflate(R.layout.match_details, view.match_details_frame, false)
            }
            view.match_details_frame.addView(layout)
            layout
        } else {
            view.match_details_frame.match_details_content
        }) as ViewGroup

        linearLayout.match_date_time.text = printMatchTime(match)

        val mapTable = linearLayout.map_result_table
        val renderRequired = (mapTable.tag as? List<*>)?.let {
            val renderedMaps = it.filterIsInstance<MatchMap>()
            match.maps != renderedMaps
        } ?: true
        if (renderRequired) {
            Log.i(TAG, "render maps at $adapterPosition")
            mapTable.removeAllViews()

            val resourceLoader = (context as BracketActivity).resourceLoader
            for (map in match.maps) {
                //attachToRoot = false is used to getTournament rendered mapView instead of its parent as returned value
                val mapViewHolder = MapViewHolder(
                    inflater.inflate(R.layout.map_result, mapTable, false), resourceLoader
                )
                mapViewHolder.display(map, match)
                mapTable.addView(mapViewHolder.view)
            }
            mapTable.tag = match.maps
        }
    }
}