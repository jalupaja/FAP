package com.example.fap.utils

import android.content.Context

class SharedCurrencyManager(context: Context) {

    private var currency = '€'

    fun num2Money(num: Number): String {
        return "%.2f".format(num) + currency
    }

    companion object {
        private var instance: SharedCurrencyManager? = null

        fun getInstance(context: Context): SharedCurrencyManager {
            if (instance == null) {
                instance = SharedCurrencyManager(context.applicationContext)
            }
            return instance as SharedCurrencyManager
        }
    }
}