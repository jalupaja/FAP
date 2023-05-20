package com.example.fap.utils

import android.content.Context
import android.content.SharedPreferences

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
        return sharedPreferences.getInt(key, defaultValue) ?: defaultValue
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
