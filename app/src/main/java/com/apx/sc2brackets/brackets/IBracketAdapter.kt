package com.apx.sc2brackets.brackets

import android.view.LayoutInflater

interface IBracketAdapter {
    val loader: MyResourceLoader

    fun inflateLayout(cb: (LayoutInflater) -> Unit)
    fun notifyItemChanged(position: Int)
    fun selectMatch(match: Match)
}