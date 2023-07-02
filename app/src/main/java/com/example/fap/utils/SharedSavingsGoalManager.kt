package com.example.fap.utils

import android.content.Context
import com.example.fap.data.FapDatabase
import com.example.fap.data.entities.Payment
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import java.util.Date

class SharedSavingsGoalManager {
    enum class TimeSpan(val label: String) {
        None("None"),
        Daily("Daily"),
        Weekly("Weekly"),
        Monthly("Monthly"),
        Yearly("Yearly"),
    }

    private fun calculateNextDate(date: Date, timeSpan: TimeSpan): Date {
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

            else -> {}
        }

        return calendar.time
    }

    fun timeSpanTitle(title: String, timeSpan: TimeSpan): String {
        return "$title (${timeSpan.label})"
    }

    fun removeTimSpanTitle(title: String, newTimeSpan: TimeSpan, oldTimeSpan: TimeSpan = TimeSpan.None): String {
        return if (title.endsWith(" (" + newTimeSpan.label + ")")) {
            title.substring(0, title.length - (newTimeSpan.label.length + 3))
        } else if (title.endsWith(" (" + oldTimeSpan.label + ")")) {
            title.substring(0, title.length - (oldTimeSpan.label.length + 3))
        } else {
            title
        }
    }

    suspend fun updateSavingsGoals(context: Context) {
        val db = FapDatabase.getInstance(context)
        val dbSavingsGoal = db.fapDaoSavingGoal()
        val dbPayment = db.fapDaoPayment()
        val curUser = SharedPreferencesManager.getInstance(context).getCurUser(context)
        val today = Date.from(LocalDate.now().atTime(23,59, 59).atZone(ZoneId.systemDefault()).toInstant())

        val savingsGoals = dbSavingsGoal.getSavingsGoals(curUser)

        for (savingsGoal in savingsGoals) {
            var nextDate = savingsGoal.nextDate

            while (today.after(nextDate)) {
                dbPayment.insertPayment(Payment(
                    userId = curUser,
                    wallet = savingsGoal.wallet,
                    title = timeSpanTitle(savingsGoal.title, savingsGoal.timeSpanPerTime),
                    description = savingsGoal.description,
                    price = savingsGoal.amountPerTime,
                    date = nextDate,
                    isPayment = savingsGoal.isPayment,
                    category = savingsGoal.category,
                    savingsGoalId = savingsGoal.id,
                ))

                nextDate = calculateNextDate(nextDate, savingsGoal.timeSpanPerTime)

                dbSavingsGoal.updateSavingsGoal(
                    savingsGoal.copy(
                        nextDate = nextDate
                    )
                )
            }

            if (savingsGoal.endDate == today) {
                dbSavingsGoal.deleteSavingsGoal(savingsGoal)
            }
        }
    }

    companion object {
        private var instance: SharedSavingsGoalManager? = null

        fun getInstance(): SharedSavingsGoalManager {
            if (instance == null) {
                instance = SharedSavingsGoalManager()
            }
            return instance as SharedSavingsGoalManager
        }
    }
}