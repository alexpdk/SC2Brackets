package com.apx.sc2brackets.components

import android.graphics.Color
import android.widget.ProgressBar

class IndeterminateProgressBar(private val progressBar: ProgressBar) {
    fun setup() {
        progressBar.isIndeterminate = true
        val indeterminateDrawable = progressBar.indeterminateDrawable.mutate()
        indeterminateDrawable.setColorFilter(Color.rgb(25, 118, 210), android.graphics.PorterDuff.Mode.SRC_IN)
        progressBar.indeterminateDrawable = indeterminateDrawable
    }
}