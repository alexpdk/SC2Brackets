package com.apx.sc2brackets.brackets

import android.content.Context
import android.support.v4.content.ContextCompat
import com.apx.sc2brackets.R

class MyResourceLoader(val context: Context){

    //load images in pre-21 api compatible way and store to reuse inside view-holders
    //May be store them in ViewModel?
    private val terranLogo = ContextCompat.getDrawable(context, R.drawable.ic_terran)!!
    private val protossLogo = ContextCompat.getDrawable(context, R.drawable.ic_protoss)!!
    private val zergLogo = ContextCompat.getDrawable(context, R.drawable.ic_zerg)!!

    private val terranWin = ContextCompat.getDrawable(context, R.drawable.ic_knuckle)!!
    private val protossWin = ContextCompat.getDrawable(context, R.drawable.ic_star)!!
    private val zergWin = ContextCompat.getDrawable(context, R.drawable.ic_fang)!!

    private val _spaceLogo = ContextCompat.getDrawable(context, R.drawable.ic_almost_blank)!!

    fun getColor(colorID: Int) = ContextCompat.getColor(context, colorID)

    fun getRaceLogo(race: Match.Race) = when (race) {
        Match.Race.PROTOSS -> protossLogo
        Match.Race.ZERG -> zergLogo
    }

    fun getSpaceLogo() = _spaceLogo

    fun getWinColor(race: Match.Race) = when(race){
        Match.Race.PROTOSS -> ContextCompat.getColor(context, R.color.protossColor)
        Match.Race.ZERG -> ContextCompat.getColor(context, R.color.zergColor)
    }

    fun getWinLogo(race: Match.Race)= when (race) {
        Match.Race.PROTOSS -> protossWin
        Match.Race.ZERG -> zergWin
    }
}