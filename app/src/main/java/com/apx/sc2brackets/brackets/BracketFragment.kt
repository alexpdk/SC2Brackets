package com.apx.sc2brackets.brackets

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.support.v7.widget.SimpleItemAnimator
import com.apx.sc2brackets.R

/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the
 * [BracketFragment.OnMatchInteractionListener] interface.
 */
class BracketFragment : Fragment() {

    private var listener: OnMatchInteractionListener? = null

    //TODO: on screen rotation this reference is lost, need a way to correctly store/pass it
    var sharedRecycledViewPool: RecyclerView.RecycledViewPool? =null

    private val timeFilter: MatchBracket.TimeFilter? by lazy {
        arguments?.get(BRACKET_TIME_FILTER) as MatchBracket.TimeFilter
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_bracket, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            val loader = MyResourceLoader(context!!)
            with(view) {
                layoutManager = LinearLayoutManager(context).apply {
                    //viewHolders from detached layout are immediately available to other RecycledViews
                    //https@ //medium.com/@thagikura/reduce-the-number-of-inflation-of-viewholders-drastically-by-sharing-a-viewpool-across-multiple-249d5fc6d28
                    recycleChildrenOnDetach = true
                }
                adapter =
                    //TODO: do sth about these arguments
                    BracketRecyclerViewAdapter(
                        MatchBracket.DEFAULT_TOURNAMENT,
                        timeFilter,
                        context,
                        loader,
                        listener
                    )
                //disable item blinking on expand/collapse
                //source: https://medium.com/@nikola.jakshic/how-to-expand-collapse-items-in-recyclerview-49a648a403a6
                (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
                setRecycledViewPool(sharedRecycledViewPool)
            }
        }
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnMatchInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnMatchInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.*/
    interface OnMatchInteractionListener {
        fun onMatchSelect(item: Match?)
    }

    companion object {
        const val BRACKET_TIME_FILTER = "BRACKET_TIME_FILTER"

        fun newInstance(timeFilter: MatchBracket.TimeFilter? = null): BracketFragment{
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
