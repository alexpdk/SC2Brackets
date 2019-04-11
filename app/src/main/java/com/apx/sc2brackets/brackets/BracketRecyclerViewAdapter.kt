package com.apx.sc2brackets.brackets

import android.content.Context
import android.graphics.Paint
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.apx.sc2brackets.R

import com.apx.sc2brackets.brackets.BracketFragment.OnMatchInteractionListener

import kotlinx.android.synthetic.main.match_item.view.*
import kotlinx.android.synthetic.main.bracket_header.view.*
import java.lang.RuntimeException

private const val TAG = "BracketViewAdapter"

/**
 * [RecyclerView.Adapter] that can display a [MatchBracket] and makes a call to the
 * specified [OnMatchInteractionListener].
 */
class BracketRecyclerViewAdapter(
    private val bracket: MatchBracket,
    private val timeFilter: MatchBracket.TimeFilter?,
    context: Context,
    private val interactionListener: OnMatchInteractionListener?
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    //load images in pre-21 api compatible way and store to reuse inside view-holders
    private val terranLogo = ContextCompat.getDrawable(context, R.drawable.ic_terran)
    private val protossLogo = ContextCompat.getDrawable(context, R.drawable.ic_protoss)
    private val zergLogo = ContextCompat.getDrawable(context, R.drawable.ic_zerg)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder{
        //viewType is layout item id
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return when(viewType) {
            R.layout.match_item -> MatchViewHolder(view)
            R.layout.bracket_header -> HeaderViewHolder(
                view
            )
            else->throw RuntimeException("Unknown viewType, not a layout id")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val bracketItem = bracket.filter(timeFilter)[position]
        when (bracketItem) {
            is Match -> {
                val mvHolder = holder as MatchViewHolder
                mvHolder.display(bracketItem)

                mvHolder.view.apply {
                    setBackgroundColor(ContextCompat.getColor(context,
                        if (position % 2 == 1) {R.color.colorPrimaryLight}else{R.color.bracketBackground}
                    ))
                    //allows backward navigation to match as item list is interacted with
                    tag = bracketItem
                }
            }
            is MatchBracket.Header -> {
                (holder as HeaderViewHolder).display(bracketItem)
            }
        }
    }

    override fun getItemCount(): Int = bracket.filter(timeFilter).size

    /*Return layout id which will be used to render corresponding items*/
    override fun getItemViewType(position: Int) = when (bracket.filter(timeFilter)[position]) {
        is Match -> R.layout.match_item
        is MatchBracket.Header -> R.layout.bracket_header
        else -> throw RuntimeException("Unknown type of item in MatchBracket: ${bracket.filter(timeFilter)[position].javaClass.name}")
    }

    class HeaderViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        fun display(header: MatchBracket.Header) {
            view.header_text.text = header.content
        }
    }

    inner class MatchViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        private val onRaceButtonClick = { _: View ->
            Log.i(TAG, "RaceButton clicked")
            Unit
        }

        private val onViewClick = { v: View ->
            Log.i(TAG, "Clicked view $v with id ${v.id}")
            val item = v.tag as Match
            item.detailsExpanded = !item.detailsExpanded

            // Notify the active callbacks interface (the activity, if the fragment is attached to
            // one) that an item has been selected.
            interactionListener?.onMatchSelect(item)

            // Notify adapter that item should be rebind with view after change
            //TODO: study and use payload parameter for efficient rebind
            notifyItemChanged(adapterPosition)
        }

        init {
            view.setOnClickListener(onViewClick)
            view.first_player_button.setOnClickListener(onRaceButtonClick)
            view.second_player_button.setOnClickListener(onRaceButtonClick)
        }

        fun display(match: Match) {
            //TODO: fix that binding cache is not used
            //Detailed info: https://antonioleiva.com/kotlin-android-extensions/

            view.first_player_name.text = match.firstPlayer
            view.second_player_name.text = match.secondPlayer
            view.match_score.text = match.score.run { "$first:$second" }

            view.first_player_button.setImageDrawable(
                when (match.races.first) {
                    Match.Race.ZERG -> zergLogo
                    Match.Race.PROTOSS -> protossLogo
                }
            )
            view.second_player_button.setImageDrawable(
                when (match.races.second) {
                    Match.Race.ZERG -> zergLogo
                    Match.Race.PROTOSS -> protossLogo
                }
            )
            view.time_button?.apply {
                text = match.countDown
                paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
            }
            view.match_details?.visibility = if (match.detailsExpanded) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }
}
