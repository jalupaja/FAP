package com.example.fap.utils

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
<<<<<<< Updated upstream
=======
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.example.fap.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter
>>>>>>> Stashed changes

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

<<<<<<< Updated upstream
=======
    fun saveCurUser(context: Context, curUser: String) {
        saveString(context.getString(R.string.shared_prefs_cur_user), curUser)
    }

    fun getCurUser(context: Context): String {
        return getString(context.getString(R.string.shared_prefs_cur_user))
    }

    private fun getDate(): String {
        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return currentDate.format(formatter)
    }

    fun saveLastCurrencyUpdate(context: Context, curDate: String = getDate()) {
        saveString(context.getString(R.string.shared_prefs_last_currency_update), curDate)
    }

    fun isCurrencyUpToDate(context: Context): Boolean {
        return (getString(context.getString(R.string.shared_prefs_last_currency_update), "0000-00-00")) == getDate()
    }

    fun saveCurrency(context: Context, currency: String) {
        saveString(context.getString(R.string.shared_prefs_current_currency), currency)
    }

    fun getCurrency(context: Context): String {
        return getString(context.getString(R.string.shared_prefs_current_currency), "€")
    }

    fun saveTheme(context: Context, newTheme: String = ""): Int {
        saveString(context.getString(R.string.shared_prefs_theme), newTheme)
        return getTheme(context)
    }

    fun getTheme(context: Context): Int {
        Log.d("Theme", getString(context.getString(R.string.shared_prefs_theme)))

        when (getString(context.getString(R.string.shared_prefs_theme))) {
            context.getString(R.string.theme_light) -> {
                Log.d("Theme", "l")
                return R.style.Theme_FAP_Light
            }

            context.getString(R.string.theme_dark) -> {
                Log.d("Theme", "d")
                return R.style.Theme_FAP_Dark
            }

            context.getString(R.string.theme_oled) -> {
                Log.d("Theme", "o")
                return R.style.Theme_FAP_Oled
            }

            else -> {
                Log.d("Theme", "default")
                return R.style.Theme_FAP_Dark
            }
        }
    }

>>>>>>> Stashed changes
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
