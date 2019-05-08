package com.apx.sc2brackets.brackets

import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.apx.sc2brackets.R
import com.apx.sc2brackets.models.Match

import kotlinx.android.synthetic.main.match_details.view.*
import kotlinx.android.synthetic.main.match_item.view.*

private const val TAG = "MatchViewHolder"

/**[MatchViewHolder] represents reusable ViewHolder inside the pool, shared by several [BracketFragment]
 * instances. It is not implemented as inner non-static class to avoid issues with referencing one [BracketFragment]
 * from another one.
 *
 * However, [MatchViewHolder] references [IBracketAdapter] object, responsible for performing context-dependent actions.
 * Reference is established via [setAdapter] method ans should be nullified as soon as ViewHolder is released after usage
 * in fragment with [clearAdapter].*/
class MatchViewHolder(val view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {

    private var bracketAdapter: IBracketAdapter? = null

    private val onRaceButtonClick = { button: View ->
        Log.i(TAG, "RaceButton clicked")
        val match = view.tag as Match
        val player = if(button==view.first_player_button){
            match.firstPlayer
        }else{
            match.secondPlayer
        }
        bracketAdapter?.interactionListener?.onPlayerSelect(player)
        Unit
    }

    private val onViewClick = { v: View ->
        val item = v.tag as Match
        item.detailsExpanded = !item.detailsExpanded

        // Notify adapter that item should be rebind with view after change
        //TODO: study and use payload parameter for efficient rebind
        bracketAdapter?.apply { notifyItemChanged(adapterPosition) } ?: Log.e(
            TAG,
            "IBracketAdapter is not specified, cannot apply changes to item"
        )

        if (item.detailsExpanded) {
            view.match_details_frame?.let {
                inflateDetails(it)
            }
        }
    }

    init {
        view.setOnClickListener(onViewClick)
        view.first_player_button.setOnClickListener(onRaceButtonClick)
        view.go_to_tournament.setOnClickListener(onRaceButtonClick)
    }

    fun clearAdapter() {
        bracketAdapter = null
    }

    fun display(match: Match) {
        //TODO: fix that binding cache is not used
        //Detailed info: https://antonioleiva.com/kotlin-android-extensions/
        view.first_player_name.text = match.firstPlayer.name
        view.second_player_name.text = match.secondPlayer.name
        view.match_score.text = match.score.run { "$first:$second" }

        view.first_player_button.setImageDrawable(
            bracketAdapter?.loader?.getRaceLogo(match.firstPlayer.race)
        )
        view.go_to_tournament.setImageDrawable(
            bracketAdapter?.loader?.getRaceLogo(match.secondPlayer.race)
        )
//        view.time_button?.apply {
//            text = match.countDown
//            paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
//        }
        view.match_details_frame?.visibility = if (match.detailsExpanded) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    private fun inflateDetails(detailsLayout: ViewGroup) {
        if (detailsLayout.match_details_content == null) {

            // add additional content to match details
            bracketAdapter?.inflateLayout {
                val linearLayout = it.inflate(R.layout.match_details, detailsLayout, false)
                        as ViewGroup
                val match = view.tag as Match
                linearLayout.match_date_time.text = match.startTimeString
                linearLayout.time_button2.text = match.countDown

                val mapList = linearLayout.map_result_table
                for (map in match.maps) {
                    //attachToRoot = false is used to get rendered mapView instead of its parent as returned value
                    val mapViewHolder = MapViewHolder(
                        it.inflate(R.layout.map_result, mapList, false), bracketAdapter!!
                    )
                    mapViewHolder.display(map, match)
                    mapList.addView(mapViewHolder.view)
                }
                detailsLayout.addView(linearLayout)
            }
        }
    }

    /**Establish reference to [IBracketAdapter] required to perform context-dependent actions.
     * Reference should be nullified with [clearAdapter] after [MatchViewHolder] is no longer used.*/
    fun setAdapter(adapter: IBracketAdapter) {
        if (bracketAdapter != null) {
            Log.e(TAG, "Reference to previous adapter should be removed with clearAdapter!")
        }
        bracketAdapter = adapter
    }
}