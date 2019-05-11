package com.apx.sc2brackets.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.apx.sc2brackets.db.TournamentDao
import com.apx.sc2brackets.db.TournamentDatabase
import com.apx.sc2brackets.models.NetworkResponse
import com.apx.sc2brackets.models.Tournament
import com.apx.sc2brackets.network.TournamentDataLoader
import com.apx.sc2brackets.utils.replace
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private const val TAG = "TournamentViewModel"

class TournamentViewModel : ViewModel(), CoroutineScope by CoroutineScope(Dispatchers.IO) {

    private lateinit var dao: TournamentDao

    private val _itemChanged = MutableLiveData<Int>()
    /**Notify [com.apx.sc2brackets.adapters.TournamentsRecyclerViewAdapter] about changes in certain list item.
     * If item index == [FULL_UPDATE_INDEX], then the whole list should be updated.*/
    val itemChanged get() :LiveData<Int> = _itemChanged

    private var _itemsExpanded = emptyList<Boolean>().toMutableList()
    /**List of boolean values to mark which list items are expanded*/
    val itemExpanded: List<Boolean> get() = _itemsExpanded

    private val _networkResponse = MutableLiveData<NetworkResponse<String>>()
    /**Most recent response for network update of tournament info*/
    val networkResponse: LiveData<NetworkResponse<String>> get() = _networkResponse

    private var _tournaments = emptyList<Tournament>().toMutableList()
    val tournaments: List<Tournament> get() = _tournaments

    /**Avoid simultaneous network requests to simplify synchronization and reduce load*/
    private val updateRequestMutex = Mutex()

    /**Display tournament that was added to database inside other activity*/
    fun displayNewTournament(url: String) = launch {
        var tournament = dao.getTournament(url)
        //in case of possible synchronization issues
        if (tournament == null) {
            delay(500)
            tournament = dao.getTournament(url)
        }
        tournament?.let {
            withContext(Dispatchers.Main) {
                _tournaments.add(it)
                _itemsExpanded.add(false)
                _itemChanged.value = _tournaments.size-1
            }
        }
    }

    private suspend fun loadTournamentInfo(url: String) = updateRequestMutex.withLock {
        val loader = TournamentDataLoader(url)
        _networkResponse.postValue(loader.fetchPage())
        return@withLock loader.loadTournament()
    }

    /**Remove item from current list and from database*/
    fun removeItem(tournament: Tournament) {
        val index = _tournaments.indexOf(tournament)
        if (index >= 0) {
            _tournaments.removeAt(index)
            _itemsExpanded.removeAt(index)
            _itemChanged.value = index
            launch {
                dao.delete(tournament)
            }
        }
    }

    /**Assign database reference to ViewModel. Perform delayed request to obtain
     * full tournament list from the database.*/
    fun setDatabase(database: TournamentDatabase) {
        dao = database.dao()
        //retain list remained from previous activity start or load data from database
        if (_tournaments.isEmpty()) {
            launch {
                val list = dao.getAllTournaments()
                // set new list and update view
                withContext(Dispatchers.Main) {
                    _tournaments = list.toMutableList()
                    _itemsExpanded = Array(_tournaments.size) { false }.toMutableList()
                    _itemChanged.value = FULL_UPDATE_INDEX
                }
            }
        }
    }

    fun toggleExpand(itemIndex: Int) {
        _itemsExpanded[itemIndex] = !itemExpanded[itemIndex]
        _itemChanged.value = itemIndex
    }

    /**Retrieve tournament info from network and update tournament in list and database*/
    fun updateTournament(url: String) = launch {
        val newTournament = loadTournamentInfo(url)
        if (newTournament != null) {

            //update tournament list in main thread
            val success = withContext(Dispatchers.Main) {
                //if oldTournament is not found in the list, then it was removed by user
                val oldTournament = _tournaments.find { it.url == url }
                val index = oldTournament?.let { _tournaments.replace(it, newTournament) } ?: -1
                if (index >= 0) {
                    _itemChanged.value = index
                    true
                } else false
            }
            //update database
            if (success) {
                dao.updateIfExists(url, newTournament)
            }
        }
    }

    companion object {
        /**Special value for [itemChanged] LiveData. Indicates that adapter should update the whole list,
         * not some specific item.*/
        const val FULL_UPDATE_INDEX = -55
    }
}
