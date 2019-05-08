package com.apx.sc2brackets.brackets

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.apx.sc2brackets.R
import com.apx.sc2brackets.models.Player
import com.apx.sc2brackets.models.Player.Race.*

class MyResourceLoader(val context: Context) {

    //load images in pre-21 api compatible way and store to reuse inside view-holders
    //May be store them in ViewModel?
    private val terranLogo: Drawable by lazy{ContextCompat.getDrawable(context, R.drawable.ic_terran)!!}
    private val protossLogo: Drawable by lazy{ContextCompat.getDrawable(context, R.drawable.ic_protoss)!!}
    private val zergLogo: Drawable by lazy{ContextCompat.getDrawable(context, R.drawable.ic_zerg)!!}

    private val terranWin: Drawable by lazy{ContextCompat.getDrawable(context, R.drawable.ic_knuckle)!!}
    private val protossWin: Drawable by lazy{ContextCompat.getDrawable(context, R.drawable.ic_star)!!}
    private val zergWin: Drawable by lazy{ContextCompat.getDrawable(context, R.drawable.ic_fang)!!}

    private val _blank: Drawable by lazy{ContextCompat.getDrawable(context, R.drawable.ic_almost_blank)!!}

    fun getColor(colorID: Int) = ContextCompat.getColor(context, colorID)

    fun getBlankLogo() = _blank

    fun getRaceBackgroundColor(race: Player.Race) = when (race) {
        PROTOSS -> ContextCompat.getColor(context, R.color.protossBackground)
        ZERG -> ContextCompat.getColor(context, R.color.zergBackground)
        TERRAN -> ContextCompat.getColor(context, R.color.terranBackground)
        TBD -> ContextCompat.getColor(context, R.color.neutralBackground)
    }

    fun getRaceColor(race: Player.Race) = when (race) {
        PROTOSS -> ContextCompat.getColor(context, R.color.protossColor)
        ZERG -> ContextCompat.getColor(context, R.color.zergColor)
        TERRAN -> ContextCompat.getColor(context, R.color.terranColor)
        TBD -> ContextCompat.getColor(context, R.color.colorAccent)
    }

    fun getRaceLogo(race: Player.Race) = when (race) {
        PROTOSS -> protossLogo
        ZERG -> zergLogo
        TERRAN -> terranLogo
        TBD -> _blank
    }

    fun getRaceWinLogo(race: Player.Race) = when (race) {
        PROTOSS -> protossWin
        ZERG -> zergWin
        TERRAN -> terranWin
        TBD -> _blank
    }
}