package com.apx.sc2brackets.components

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import com.apx.sc2brackets.R

class FinishAlert(private val activity: Activity, private val msg: String) {
    fun show() {
        if (!activity.isFinishing && !activity.isDestroyed) {
            AlertDialog.Builder(activity, R.style.Theme_AppCompat_Dialog_MinWidth)
                .setMessage(msg)
                .setPositiveButton("Back") { dialog, _ ->
                    dialog.dismiss()
                }
                                                                                                                                                                                         .setCancelable(false)
                .setOnDismissListener { activity.finish() }
                .show()
        }
    }
}