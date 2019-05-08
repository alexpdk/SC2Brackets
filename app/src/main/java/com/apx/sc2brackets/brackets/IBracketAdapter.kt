package com.apx.sc2brackets.brackets

import android.view.LayoutInflater

interface IBracketAdapter {
    val interactionListener: BracketFragment.OnMatchInteractionListener
    val loader: MyResourceLoader

    fun inflateLayout(cb: (LayoutInflater) -> Unit)
    fun notifyItemChanged(position: Int)
}