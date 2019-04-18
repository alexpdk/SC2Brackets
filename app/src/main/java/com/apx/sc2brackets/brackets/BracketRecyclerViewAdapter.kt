package com.apx.sc2brackets.brackets

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.apx.sc2brackets.R

import com.apx.sc2brackets.brackets.BracketFragment.OnMatchInteractionListener

import kotlinx.android.synthetic.main.bracket_header.view.*
import kotlinx.android.synthetic.main.match_item.view.*
import java.lang.RuntimeException

private const val TAG = "BracketViewAdapter"

/**
 * [RecyclerView.Adapter] that can display a [MatchBracket] and makes a call to the
 * specified [OnMatchInteractionListener].
 */
class BracketRecyclerViewAdapter(
    private val bracket: MatchBracket,
    private val timeFilter: MatchBracket.TimeFilter?,
    private val context: Context,
    override val loader: MyResourceLoader,
    private val interactionListener: OnMatchInteractionListener?
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), IBracketAdapter {

    override fun inflateLayout(cb: (LayoutInflater) -> Unit) {
        cb(LayoutInflater.from(context))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
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
                // check ViewHolder is completely recycled, not just rebound
                // tag reference is manually reset inside onViewRecycled
                if(mvHolder.view.tag == null){
                    mvHolder.setAdapter(this)
                }
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

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        if(holder is MatchViewHolder){
            holder.clearAdapter()
            //Removing tag is important, it allows to define if ViewHolder was recycled or just rebound
            holder.view.tag = null
            holder.view.match_details_frame?.removeAllViews()
        }
    }

    override fun getItemCount(): Int = bracket.filter(timeFilter).size

    /*Return layout id which will be used to render corresponding items*/
    override fun getItemViewType(position: Int) = when (bracket.filter(timeFilter)[position]) {
        is Match -> R.layout.match_item
        is MatchBracket.Header -> R.layout.bracket_header
        else -> throw RuntimeException("Unknown type of item in MatchBracket: ${bracket.filter(timeFilter)[position].javaClass.name}")
    }

    override fun selectMatch(match: Match) {
        // Notify the active callbacks interface (the activity, if the fragment is attached to
        // one) that an item has been selected.
        interactionListener?.onMatchSelect(match)
    }

    class HeaderViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        fun display(header: MatchBracket.Header) {
            view.header_text.text = header.content
        }
    }
}
