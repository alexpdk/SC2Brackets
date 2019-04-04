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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.match_item_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                layoutManager = LinearLayoutManager(context)
                adapter =
                    BracketRecyclerViewAdapter(
                        MatchBracket.DEFAULT_TOURNAMENT,
                        context,
                        listener
                    )
                //disable item blinking on expand/collapse
                //source: https://medium.com/@nikola.jakshic/how-to-expand-collapse-items-in-recyclerview-49a648a403a6
                (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
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

/*    companion object {
        const val ARG_COLUMN_COUNT = "column-count"

        @JvmStatic
        fun newInstance(columnCount: Int) =
            BracketFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }*/
}
