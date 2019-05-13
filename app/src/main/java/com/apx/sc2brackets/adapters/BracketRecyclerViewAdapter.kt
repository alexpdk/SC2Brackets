package com.apx.sc2brackets.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.apx.sc2brackets.R

import com.apx.sc2brackets.components.MatchViewHolder
import com.apx.sc2brackets.models.Match
import com.apx.sc2brackets.models.MatchBracket

import kotlinx.android.synthetic.main.bracket_header.view.*
import java.lang.RuntimeException

private const val UNDEFINED_VIEW_TYPE = 0
private val TAG = "BracketRecyclerViewAdapter".substring(0..22)

class BracketRecyclerViewAdapter(
    private val timeFilter: MatchBracket.TimeFilter?,
    private val context: Context
) : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {

    private var bracket: MatchBracket? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): androidx.recyclerview.widget.RecyclerView.ViewHolder {
        //viewType is layout item id
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return when (viewType) {
            R.layout.match_item -> MatchViewHolder(view, context)
            R.layout.bracket_header -> HeaderViewHolder(view)
            UNDEFINED_VIEW_TYPE -> throw RuntimeException("ViewHolder created for undefined bracket")
            else -> throw RuntimeException("Unknown viewType, not a layout id")
        }
    }

    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        val bracketItem = bracket?.filter(timeFilter)?.get(position)
        when (bracketItem) {
            is Match -> {
                val mvHolder = holder as MatchViewHolder
                mvHolder.setAdapter(this)
                mvHolder.onBind(bracketItem, position)
            }
            is MatchBracket.Header -> {
                (holder as HeaderViewHolder).onBind(bracketItem)
            }
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

    fun setBracket(newBracket: MatchBracket) {
        bracket = newBracket
        notifyDataSetChanged()
    }

    class HeaderViewHolder(private val view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        fun onBind(header: MatchBracket.Header) {
            view.header_text.text = header.content
        }
    }
}
