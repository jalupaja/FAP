package com.example.fap.utils

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.fap.data.AppDatabase
import com.example.fap.data.UserDao
import com.example.fap.R
import com.example.fap.data.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

//import net.sqlcipher.database.SupportFactory

object SharedDatabaseManager {
    private var instance: SharedDatabaseManager? = null
    private lateinit var sharedPreferences: SharedPreferencesManager
    private lateinit var context: Context
    private lateinit var database: AppDatabase

    suspend fun setupDefaultValues() {
        // TODO
    }

    /*
    suspend fun getTotal(): Double {
        val payments = database.userDao().getPayments(getCurrentUser())
        var amount = 0.0
        for (payment in payments) {
            if (payment.isPayment) {
                amount -= payment.price
            } else {
                amount += payment.price
            }
        }
        return amount
    }
     */

    private fun getCurrentUser(): String {
        return sharedPreferences.getString(context.getString(R.string.shared_prefs_cur_user))
    }

    fun getInstance(context: Context, password: String = ""): SharedDatabaseManager {
        return instance ?: synchronized(this) {
            if (password != "") {
                database = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    context.getString(R.string.shared_prefs_database_name)
                ).build()
            }
            sharedPreferences = SharedPreferencesManager.getInstance(context)
            this.context = context
            instance = this
            return this
        }
    }

    fun getInstance2(context: Context, password: String = ""): SharedDatabaseManager {
        return instance ?: synchronized(this) {

            if (password != "") {
                // TODO use this one?
                //  val passphrase: ByteArray = SQLiteDatabase.getBytes(password)
                //val factory = SupportFactory(password.toByteArray())
                database = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    context.getString(R.string.shared_prefs_database_name)
                )
                    // TODO uncomment to use sqlcipher
                    // .openHelperFactory(factory)
                    .build()
                sharedPreferences = SharedPreferencesManager.getInstance(context)
                this.context = context
                //dao = appDatabase.userDao()
            }
            instance = this
            return this
        }
    }
}
