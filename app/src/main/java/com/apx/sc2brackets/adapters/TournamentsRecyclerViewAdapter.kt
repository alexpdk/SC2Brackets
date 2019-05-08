package com.apx.sc2brackets.adapters

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.apx.sc2brackets.activities.MainActivity
import com.apx.sc2brackets.R
import com.apx.sc2brackets.SC2BracketsApplication
import com.apx.sc2brackets.models.Tournament
import kotlinx.android.synthetic.main.tournament_item.view.*
import kotlinx.android.synthetic.main.tournaments_header.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.RuntimeException

private val TAG = "TournamentsRecyclerViewAdapter".substring(0..22)

class TournamentsRecyclerViewAdapter(private val applicationContext: Context) :
    androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {

    private val dao = (applicationContext as SC2BracketsApplication).tournamentDatabase.tournamentDao()
    var navigator: TournamentNavigator? = null
    private var tournaments = emptyList<Tournament>().toMutableList()
    //TODO: use MyResourceLoader, move it to application
    private val buttonIcon = ContextCompat.getDrawable(applicationContext, R.drawable.ic_right_arrow)!!

    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        if (position > 0) {
            val tournament = tournaments[position - 1]
            holder.itemView.tag = tournament
            (holder as? TournamentViewHolder)?.apply { display(tournament, buttonIcon) }

            holder.itemView.go_to_tournament.setOnClickListener(onItemButtonClick)
        } else {
            (holder as? HeaderViewHolder)?.apply { display() }
            holder.itemView.find_tournament.setOnClickListener(onHeaderButtonClick)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): androidx.recyclerview.widget.RecyclerView.ViewHolder {
        val view = LayoutInflater.from(applicationContext).inflate(viewType, parent, false)
        Log.i(TAG, "ViewHolder created")
        return when (viewType) {
            R.layout.tournament_item -> TournamentViewHolder(
                view
            )
            R.layout.tournaments_header -> HeaderViewHolder(
                view
            )
            else -> throw RuntimeException("Unknown viewType, not a layout id")
        }
    }

    private val onHeaderButtonClick = View.OnClickListener { view: View ->
        val parent = view.parent
        if (parent is View) {
            navigator?.goAndSave(Tournament(name = "", url = MainActivity.BASE_URL + parent.tournament_url.text))
        }
    }

    private val onItemButtonClick = View.OnClickListener { view: View ->
        val parent = view.parent
        if (parent is View) {
            val tag = parent.tag
            if (tag is Tournament) {
                navigator?.goToTournament(tag)
            }
        }
    }

    override fun getItemCount() = tournaments.size + 1

    override fun getItemViewType(position: Int) = when (position) {
        0 -> R.layout.tournaments_header
        else -> R.layout.tournament_item
    }

    fun remove(position: Int) {
        if (position > 0) {
            //0 position is for list header
            val tournamentIndex = position - 1
            val removedTournament = tournaments.removeAt(tournamentIndex)
            notifyItemRemoved(position)
            GlobalScope.launch(Dispatchers.IO) {
                dao.delete(removedTournament)
            }
        }
    }

    fun setData(tournaments: List<Tournament>) {
        this.tournaments = tournaments.toMutableList()
        notifyDataSetChanged()
    }

    class HeaderViewHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        fun display() {
            itemView.find_tournament.isEnabled = false
            val urlText = itemView.tournament_url
            //fix from here: https://stackoverflow.com/questions/13614101/fatal-crash-focus-search-returned-a-view-that-wasnt-able-to-take-focus
            urlText.imeOptions = EditorInfo.IME_ACTION_DONE

            urlText.addTextChangedListener(TextChangeWatcher {
                itemView.find_tournament.isEnabled = urlText.text.isNotBlank()
            })
        }
    }

    class TournamentViewHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        fun display(tournament: Tournament, buttonIcon: Drawable) {
            itemView.tournament_name.text = tournament.name
            itemView.go_to_tournament.setImageDrawable(buttonIcon)
        }
    }

    class TextChangeWatcher(private val onChange: () -> Unit) : TextWatcher {
        override fun afterTextChanged(s: Editable?) {}

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = onChange()
    }
}