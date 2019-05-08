package com.apx.sc2brackets.adapters

import com.apx.sc2brackets.models.Tournament

interface TournamentNavigator {
    fun goToTournament(tournament: Tournament)
    fun goAndSave(tournament: Tournament)
}