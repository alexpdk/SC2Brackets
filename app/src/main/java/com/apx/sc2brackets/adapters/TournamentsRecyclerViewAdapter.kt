package com.apx.sc2brackets.adapters

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.content.ContextCompat
import com.apx.sc2brackets.activities.MainActivity
import com.apx.sc2brackets.R
import com.apx.sc2brackets.utils.countDown
import com.apx.sc2brackets.models.Tournament
import com.apx.sc2brackets.utils.timeDifference
import com.apx.sc2brackets.view_models.TournamentViewModel
import kotlinx.android.synthetic.main.tournament_info.view.*
import kotlinx.android.synthetic.main.tournament_item.view.*
import kotlinx.android.synthetic.main.tournaments_header.view.*
import org.joda.time.DateTime
import java.lang.RuntimeException

private val TAG = "TournamentsRecyclerViewAdapter".substring(0..22)

class TournamentsRecyclerViewAdapter(
    private val activityContext: Context,
    private val tournamentViewModel: TournamentViewModel
) :
    androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {

    //TODO: remove navigator, call activity directly
    var navigator: TournamentNavigator? = null

    //TODO: use MyResourceLoader, move it to application
    private val buttonIcon = ContextCompat.getDrawable(activityContext, R.drawable.ic_right_arrow)!!

    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        Log.i(TAG, "onBindViewHolder position = $position")
        if (position > 0) {
            val tournament = tournamentViewModel.tournaments[position - 1]
            val tagChanged = holder.itemView.tag != tournament
            holder.itemView.tag = tournament
            (holder as? TournamentViewHolder)?.apply { onBind(tournament, tagChanged = tagChanged) }
        } else {
            (holder as? HeaderViewHolder)?.apply { display() }
            holder.itemView.find_tournament.setOnClickListener(onHeaderButtonClick)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): androidx.recyclerview.widget.RecyclerView.ViewHolder {
        val view = LayoutInflater.from(activityContext).inflate(viewType, parent, false)
        return when (viewType) {
            R.layout.tournament_item -> TournamentViewHolder(view)
                .apply { onCreate(buttonIcon) }
            R.layout.tournaments_header -> HeaderViewHolder(view)
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

    override fun getItemCount() = tournamentViewModel.tournaments.size + 1

    override fun getItemViewType(position: Int) = when (position) {
        0 -> R.layout.tournaments_header
        else -> R.layout.tournament_item
    }

    fun remove(tournament: Tournament) {
        tournamentViewModel.removeItem(tournament)
    }

    fun update(tournament: Tournament) {
        (activityContext as MainActivity).updateTournament(tournament)
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

    inner class TournamentViewHolder(view: View) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {

        private var tournamentInfo: View? = null

        fun onCreate(buttonIcon: Drawable) {
            with(itemView.second_player_button) {
                setImageDrawable(buttonIcon)
                setOnClickListener(onItemButtonClick)
            }
            itemView.setOnClickListener { tournamentViewModel.toggleExpand(adapterPosition - 1) }
        }

        fun onBind(tournament: Tournament, tagChanged: Boolean) {
            itemView.tournament_name.text = tournament.name
            if (tournamentViewModel.itemExpanded[adapterPosition - 1]) {
                expandInfo()
            } else {
                collapseInfo()
            }
            if(tagChanged) tournamentInfo?.run{
                bracket_button.setOnClickListener { navigator?.goToTournament(itemView.tag as Tournament) }
                update_button.setOnClickListener { update(itemView.tag as Tournament) }
            }
        }

        private fun expandInfo() {
            if (tournamentInfo == null) {
                tournamentInfo = LayoutInflater.from(activityContext)
                    .inflate(R.layout.tournament_info, itemView.tournament_info_frame, false)
                itemView.tournament_info_frame.addView(tournamentInfo)

                tournamentInfo?.notifications_switch?.thumbTintList =
                    ContextCompat.getColorStateList(activityContext, R.color.switch_color)
            }
            itemView.tournament_info_frame.visibility = VISIBLE
            tournamentInfo?.run{
                // Set last update time
                last_update_time.text = tournament.entity.lastUpdate.let {
                    "${timeDifference(DateTime.now(), it)} ago"
                }
                //Set next match text
                next_game.text = tournament.nextMatch()?.let {
                    countDown(it.startTime)
                        .plus("\n${it.firstPlayer.name} vs ${it.secondPlayer.name}")
                } ?: "No next match"
            }
        }

        private fun collapseInfo() {
            itemView.tournament_info_frame.visibility = GONE
        }

        val tournament get() = itemView.tag as Tournament
    }

    class TextChangeWatcher(private val onChange: () -> Unit) : TextWatcher {
        override fun afterTextChanged(s: Editable?) {}

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = onChange()
    }
}