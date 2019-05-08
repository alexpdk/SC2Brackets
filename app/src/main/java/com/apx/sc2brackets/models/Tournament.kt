package com.apx.sc2brackets.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Tournament(
    val name: String?,
    @PrimaryKey val url: String)
{
    companion object {
        val DEFAULT_KNOWN_LIST = listOf(
            Tournament(
                name = "2019 WCS Spring: Europe Qualifier",
                url = "https://liquipedia.net/starcraft2/2019_WCS_Spring/Qualifiers/Europe"
            ),
            Tournament(
                name = "2019 WCS Spring: North America Qualifier",
                url = "https://liquipedia.net/starcraft2/2019_WCS_Spring/Qualifiers/North_America"
            ),
            Tournament(
                name = "IEM Season XIII - Katowice",
                url = "https://liquipedia.net/starcraft2/IEM_Season_XIII_-_Katowice"
            ),
            Tournament(
                name = "Red Hot Cup #1",
                url = "https://liquipedia.net/starcraft2/Red_Hot_Cup/1"
            )
        )
    }
}