package com.apx.sc2brackets.brackets

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.apx.sc2brackets.R
import com.apx.sc2brackets.adapters.BracketRecyclerViewAdapter
import com.apx.sc2brackets.models.MatchBracket
import com.apx.sc2brackets.models.Player
import com.apx.sc2brackets.view_models.BracketViewModel

private const val TAG = "BracketFragment"

/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the
 * [BracketFragment.OnMatchInteractionListener] interface.
 */
class BracketFragment : Fragment() {

    private var matchInteractionListener: OnMatchInteractionListener? = null

    //TODO: on screen rotation this reference is lost, need a way to correctly store/pass it
    var sharedRecycledViewPool: androidx.recyclerview.widget.RecyclerView.RecycledViewPool? = null

    private val timeFilter: MatchBracket.TimeFilter? by lazy {
        arguments?.get(BRACKET_TIME_FILTER) as MatchBracket.TimeFilter
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_bracket, container, false)

        // Set the adapter
        if (view is androidx.recyclerview.widget.RecyclerView) {
            val loader = MyResourceLoader(context!!)
            //TODO: do sth about these arguments
            val bracketAdapter = BracketRecyclerViewAdapter(
                timeFilter,
                context!!,
                loader,
                matchInteractionListener!!
            )
            with(view) {
                layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context).apply {
                    //viewHolders from detached layout are immediately available to other RecycledViews
                    //https@ //medium.com/@thagikura/reduce-the-number-of-inflation-of-viewholders-drastically-by-sharing-a-viewpool-across-multiple-249d5fc6d28
                    recycleChildrenOnDetach = true
                }
                adapter = bracketAdapter
                //disable item blinking on expand/collapse
                //source: https://medium.com/@nikola.jakshic/how-to-expand-collapse-items-in-recyclerview-49a648a403a6
                (itemAnimator as androidx.recyclerview.widget.SimpleItemAnimator).supportsChangeAnimations = false
                setRecycledViewPool(sharedRecycledViewPool)
            }
            //BracketViewModel is properly initialized in BracketActivity and just passed here
            val tournamentViewModel = ViewModelProviders.of(activity!!).get(BracketViewModel::class.java)
            tournamentViewModel.bracket.observe(this, Observer {
                Log.i(TAG, "MatchBracket updated, new value = $it")
                bracketAdapter.setBracket(it)
            })
        }
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnMatchInteractionListener) {
            matchInteractionListener = context
        } else {
            throw RuntimeException("$context must implement OnMatchInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        matchInteractionListener = null
    }

    interface OnMatchInteractionListener {
        fun onPlayerSelect(player: Player)
    }

    companion object {
        const val BRACKET_TIME_FILTER = "BRACKET_TIME_FILTER"

        fun newInstance(timeFilter: MatchBracket.TimeFilter? = null): BracketFragment {
            val args = Bundle()
            timeFilter?.let {
                args.putSerializable(BRACKET_TIME_FILTER, timeFilter)
            }
            val fragment = BracketFragment()
            fragment.arguments = args
            return fragment
        }
    }
}
