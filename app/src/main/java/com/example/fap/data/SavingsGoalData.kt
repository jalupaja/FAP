package com.example.fap.data

import android.util.Log
import java.util.Calendar
import java.util.Date

enum class TimeSpan(val label: String) {
    None("None"),
    Daily("Daily"),
    Weekly("Weekly"),
    Monthly("Monthly"),
    Yearly("Yearly"),
}

fun calculateNextDate(date: Date, timeSpan: TimeSpan): Date {
    val calendar: Calendar = Calendar.getInstance()
    calendar.time = date

    when (timeSpan) {
        TimeSpan.Daily -> {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        TimeSpan.Weekly -> {
            calendar.add(Calendar.WEEK_OF_YEAR, 1)
        }
        TimeSpan.Monthly -> {
            calendar.add(Calendar.MONTH, 1)
        }
        TimeSpan.Yearly -> {
            calendar.add(Calendar.YEAR, 1)
        }
        else -> { }
    }

    return calendar.time
}

fun timeSpanTitle(title: String, timeSpan: TimeSpan): String {
    return "$title (${timeSpan.label})"
}
