package com.apx.sc2brackets.utils

import com.apx.sc2brackets.db.DateTimeTypeConverter
import org.joda.time.*

//TODO: use Period API properly
//TODO: unite functions to Locale-dependent class
fun timeDifference(moment1: DateTime, moment2: DateTime): String {
    val dd = Math.abs(Days.daysBetween(moment1, moment2).days)
    val hh = Math.abs(Hours.hoursBetween(moment1, moment2).hours % 24)
    val mm = Math.abs(Minutes.minutesBetween(moment1, moment2).minutes % 60)
    val ss = Math.abs(Seconds.secondsBetween(moment1, moment2).seconds % 60)
    return when {
        dd > 0 -> "${dd}d ${hh}h"
        hh > 0 -> "${hh}h ${mm}m"
        mm > 0 -> "${mm}m ${ss}s"
        else -> "${ss}s"
    }
}

fun countDown(moment: DateTime?) = moment.let {
    val now = DateTime.now()
    when {
        it == null -> "Unknown time"
        it.isBefore(now) -> "${timeDifference(now, it)} ago"
        else -> "In ${timeDifference(now, it)}"
    }
}

fun stringify(moment: DateTime?) = moment?.let { DateTimeTypeConverter.toString(it) } ?: "Time is unknown"