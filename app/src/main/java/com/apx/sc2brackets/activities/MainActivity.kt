package com.apx.sc2brackets.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import com.apx.sc2brackets.R
import com.apx.sc2brackets.SC2BracketsApplication
import com.apx.sc2brackets.adapters.TournamentNavigator
import com.apx.sc2brackets.adapters.TournamentsRecyclerViewAdapter
import com.apx.sc2brackets.models.Tournament

import kotlinx.android.synthetic.main.activity_tournaments.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity(), TournamentNavigator {

    private lateinit var itemTouchHelper: ItemTouchHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tournaments)

        val adapter =
            TournamentsRecyclerViewAdapter(applicationContext = applicationContext)
        adapter.navigator = this

        tournament_list_view.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        tournament_list_view.adapter = adapter

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
                when (direction) {
                    ItemTouchHelper.LEFT -> adapter.remove(holder.adapterPosition)
                    ItemTouchHelper.RIGHT -> openBracket(holder.itemView.tag as Tournament)
                }
            }
        }

        itemTouchHelper = ItemTouchHelper(swipeCallback).apply {
            attachToRecyclerView(tournament_list_view)
        }
        GlobalScope.launch(Dispatchers.IO) {
            val dao = (applicationContext as SC2BracketsApplication).tournamentDatabase.tournamentDao()
            Log.i(TAG, "read data")
            val list = dao.getAll()

            withContext(Dispatchers.Main) {
                list.observe(this@MainActivity, Observer {
                    if(it != null){
                        Log.i(TAG, "setData, list size = ${it.size}")
                        adapter.setData(tournaments = it)
                    }
                })
            }
        }
    }

    override fun onResume() {
        super.onResume()
        //Fix, that restores swiped out items
        //https://stackoverflow.com/questions/31787272/android-recyclerview-itemtouchhelper-revert-swipe-and-restore-view-holder/37342327#37342327
        itemTouchHelper.attachToRecyclerView(null)
        itemTouchHelper.attachToRecyclerView(tournament_list_view)
    }

    override fun goToTournament(tournament: Tournament) = openBracket(tournament)

    override fun goAndSave(tournament: Tournament) = openBracket(tournament, suggestSave = true)

    private fun openBracket(tournament: Tournament, suggestSave: Boolean = false) {
        val intent = Intent(this, BracketActivity::class.java).apply {
            tournament.name?.let {
                putExtra(TOURNAMENT_NAME, it)
            }
            putExtra(TOURNAMENT_URL, tournament.url)
            putExtra(SUGGEST_SAVE, suggestSave)
        }
        startActivity(intent)
    }

    companion object {
        const val BASE_URL = "https://liquipedia.net/starcraft2/"

        const val TOURNAMENT_NAME = "TOURNAMENT_NAME"
        const val TOURNAMENT_URL = "TOURNAMENT_URL"
        const val SUGGEST_SAVE = "SUGGEST_SAVE"
    }
}