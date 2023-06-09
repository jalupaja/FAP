package com.example.fap.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.fap.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class SharedPreferencesManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("FAP_SHARED_PREFS", Context.MODE_PRIVATE)

    fun saveString(key: String, value: String) {
        val editor = sharedPreferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun getString(key: String, defaultValue: String=""): String {
        return sharedPreferences.getString(key, defaultValue) ?: defaultValue
    }

    fun saveInt(key: String, value: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt(key, value)
        editor.apply()
    }

    fun getInt(key: String, defaultValue: Int=0): Int {
        return sharedPreferences.getInt(key, defaultValue)
    }

    fun saveCurUser(context: Context, curUser: String) {
        val editor = sharedPreferences.edit()
        editor.putString(context.getString(R.string.shared_prefs_cur_user), curUser)
        editor.apply()
    }

    fun getCurUser(context: Context): String {
        return sharedPreferences.getString(context.getString(R.string.shared_prefs_cur_user), "") ?: ""
    }

    private fun getDate(): String {
        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return currentDate.format(formatter)
    }

    fun saveLastCurrencyUpdate(context: Context, curDate: String = getDate()) {
        val editor = sharedPreferences.edit()
        editor.putString(context.getString(R.string.shared_prefs_last_currency_update), curDate)
        editor.apply()
    }

    fun isCurrencyUpToDate(context: Context): Boolean {
        return (sharedPreferences.getString(
            context.getString(R.string.shared_prefs_last_currency_update),
            "0000-00-00"
        ) ?: "0000-00-00") == getDate()
    }

    fun saveCurrency(context: Context, currency: String) {
        val editor = sharedPreferences.edit()
        editor.putString(context.getString(R.string.shared_prefs_current_currency), currency)
        editor.apply()
    }

    fun getCurrency(context: Context): String {
        return sharedPreferences.getString(context.getString(R.string.shared_prefs_current_currency), "€") ?: "€"
    }

    companion object {
        private var instance: SharedPreferencesManager? = null

        fun getInstance(context: Context): SharedPreferencesManager {
            if (instance == null) {
                instance = SharedPreferencesManager(context.applicationContext)
            }
            return instance as SharedPreferencesManager
        }
    }
}
