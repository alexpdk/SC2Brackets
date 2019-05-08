package com.apx.sc2brackets.adapters

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.apx.sc2brackets.R

import com.apx.sc2brackets.brackets.BracketFragment.OnMatchInteractionListener
import com.apx.sc2brackets.brackets.IBracketAdapter
import com.apx.sc2brackets.brackets.MatchViewHolder
import com.apx.sc2brackets.brackets.MyResourceLoader
import com.apx.sc2brackets.models.Match
import com.apx.sc2brackets.models.MatchBracket

import kotlinx.android.synthetic.main.bracket_header.view.*
import kotlinx.android.synthetic.main.match_item.view.*
import java.lang.RuntimeException

private const val TAG = "BracketViewAdapter"
private const val UNDEFINED_VIEW_TYPE = 0

/**
 * [RecyclerView.Adapter] that can display a [MatchBracket] and makes a call to the
 * specified [OnMatchInteractionListener].
 */
class BracketRecyclerViewAdapter(
    private val timeFilter: MatchBracket.TimeFilter?,
    private val context: Context,
    override val loader: MyResourceLoader,
    override val interactionListener: OnMatchInteractionListener
) : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>(),
    IBracketAdapter {

    private var bracket: MatchBracket? = null

    override fun inflateLayout(cb: (LayoutInflater) -> Unit) {
        cb(LayoutInflater.from(context))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
        //viewType is layout item id
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return when(viewType) {
            R.layout.match_item -> MatchViewHolder(view)
            R.layout.bracket_header -> HeaderViewHolder(
                view
            )
            UNDEFINED_VIEW_TYPE -> throw RuntimeException("ViewHolder created for undefined bracket")
            else->throw RuntimeException("Unknown viewType, not a layout id")
        }
    }

    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        val bracketItem = bracket?.filter(timeFilter)?.get(position)
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
                        if (position % 2 == 1) {R.color.terranBackground}else{R.color.protossBackground}
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

    override fun onViewRecycled(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        if(holder is MatchViewHolder){
            holder.clearAdapter()
            //Removing tag is important, it allows to define if ViewHolder was recycled or just rebound
            holder.view.tag = null
            holder.view.match_details_frame?.removeAllViews()
        }
    }

    override fun getItemCount(): Int = bracket?.filter(timeFilter)?.size ?: 0

    /*Return layout id which will be used to render corresponding items*/
    override fun getItemViewType(position: Int) = bracket?.filter(timeFilter)?.let {
        when (it[position]) {
            is Match -> R.layout.match_item
            is MatchBracket.Header -> R.layout.bracket_header
            else -> throw RuntimeException("Unknown type of item in MatchBracket: ${it[position].javaClass.name}")
        }
    } ?: UNDEFINED_VIEW_TYPE

    fun setBracket(newBracket: MatchBracket){
        bracket = newBracket
        notifyDataSetChanged()
    }

    class HeaderViewHolder(private val view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        fun display(header: MatchBracket.Header) {
            view.header_text.text = header.content
        }
    }
}
