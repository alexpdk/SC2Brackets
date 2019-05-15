package com.apx.sc2brackets.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.apx.sc2brackets.components.IndeterminateProgressBar
import com.apx.sc2brackets.R
import com.apx.sc2brackets.SC2BracketsApplication
import com.apx.sc2brackets.adapters.TournamentsRecyclerViewAdapter
import com.apx.sc2brackets.models.Tournament
import com.apx.sc2brackets.view_models.TournamentViewModel
import com.google.android.material.snackbar.Snackbar

import kotlinx.android.synthetic.main.activity_tournaments.*

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    private lateinit var itemTouchHelper: ItemTouchHelper
    private lateinit var tournamentViewModel: TournamentViewModel

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == SAVE_NEW_TOURNAMENT_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                if (data?.hasExtra(BracketActivity.TOURNAMENT_URL) == true) {
                    //display new tournament if it was added to database
                    tournamentViewModel.displayNewTournament(data.getStringExtra(BracketActivity.TOURNAMENT_URL))
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tournaments)

        supportActionBar?.apply {
            setLogo(
                ContextCompat.getDrawable(this@MainActivity, R.mipmap.ic_launcher)
            )
            title = "Tournaments"
            setDisplayShowHomeEnabled(true)
            setDisplayUseLogoEnabled(true)
        }
        tournamentViewModel = ViewModelProviders.of(this).get(TournamentViewModel::class.java)
        val adapter =
            TournamentsRecyclerViewAdapter(
                activityContext = this,
                tournamentViewModel = tournamentViewModel
            )

        tournament_list_view.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        tournament_list_view.adapter = adapter
        setRecyclerViewBottomOffset(offsetPx = 60)

        IndeterminateProgressBar(progressBar).setup()
        setSwipeHandlers(onLeft = { tournamentViewModel.removeItem(it) }, onRight = { openBracket(it) })

        tournamentViewModel.networkResponse.observe(this, Observer {
            if (it != null && !it.isSuccessful) {
                Snackbar.make(tournament_list_view, "Network data not available", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
            }
        })
        tournamentViewModel.itemChanged.observe(this, Observer {
            progressBar.visibility = GONE
            when {
                it >= 0 -> adapter.notifyItemChanged(it + 1)//0 position is used for header
                it == TournamentViewModel.FULL_UPDATE_INDEX -> adapter.notifyDataSetChanged()
            }
        })
        tournamentViewModel.itemRemoved.observe(this, Observer {
            adapter.notifyItemRemoved(it + 1)
        })
        tournamentViewModel.setDatabase(
            (applicationContext as SC2BracketsApplication).tournamentDatabase
        )
    }

    override fun onResume() {
        super.onResume()
        Log.i(TAG, "onResume")
        //Fix, that restores swiped out items
        //https://stackoverflow.com/questions/31787272/android-recyclerview-itemtouchhelper-revert-swipe-and-restore-view-holder/37342327#37342327
        itemTouchHelper.attachToRecyclerView(null)
        itemTouchHelper.attachToRecyclerView(tournament_list_view)
        // resynchronize list data with database content
        tournamentViewModel.syncWithDatabase()
    }

    fun openBracket(tournament: Tournament, suggestSave: Boolean = false) {
        val intent = Intent(this, BracketActivity::class.java).apply {
            tournament.name?.let {
                putExtra(BracketActivity.TOURNAMENT_NAME, it)
            }
            putExtra(BracketActivity.TOURNAMENT_URL, tournament.url)
            putExtra(BracketActivity.SUGGEST_SAVE, suggestSave)
        }
        if (suggestSave) {
            startActivityForResult(intent, SAVE_NEW_TOURNAMENT_CODE)
        } else {
            startActivity(intent)
        }
    }

    //TODO: use dp or obtain comment text in px for display on different devices
    private fun setRecyclerViewBottomOffset(offsetPx: Int) {
        val offsetDecoration = object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                super.getItemOffsets(outRect, view, parent, state)
                val dataSize = state.itemCount
                val position = parent.getChildAdapterPosition(view)
                if (dataSize > 0 && position == dataSize - 1) {
                    outRect.set(0, 0, 0, offsetPx)
                } else {
                    outRect.set(0, 0, 0, 0)
                }
            }
        }
        tournament_list_view.addItemDecoration(offsetDecoration)
    }

    private fun setSwipeHandlers(onLeft: (Tournament) -> Unit, onRight: (Tournament) -> Unit) {
        val swipeCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

            //disable header swiping
            override fun getSwipeDirs(
                recyclerView: androidx.recyclerview.widget.RecyclerView,
                viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder
            ) = when (viewHolder) {
                is TournamentsRecyclerViewAdapter.HeaderViewHolder -> 0
                else -> super.getSwipeDirs(recyclerView, viewHolder)
            }

            //moving items not allowed
            override fun onMove(
                p0: androidx.recyclerview.widget.RecyclerView,
                p1: androidx.recyclerview.widget.RecyclerView.ViewHolder,
                p2: androidx.recyclerview.widget.RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, direction: Int) {
                (holder.itemView.tag as? Tournament)?.let {
                    when (direction) {
                        ItemTouchHelper.LEFT -> onLeft(it)
                        ItemTouchHelper.RIGHT -> onRight(it)
                    }
                }
            }
        }
        itemTouchHelper = ItemTouchHelper(swipeCallback).apply {
            attachToRecyclerView(tournament_list_view)
        }
    }

    fun updateTournament(tournament: Tournament) {
        progressBar.visibility = VISIBLE
        tournamentViewModel.updateTournamentAsync(tournament.url)
    }

    companion object {
        const val BASE_URL = "https://liquipedia.net/starcraft2/"
        const val SAVE_NEW_TOURNAMENT_CODE = 1122
    }
}