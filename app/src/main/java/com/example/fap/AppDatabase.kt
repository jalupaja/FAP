package com.example.fap.data

import androidx.room.Database
import androidx.room.RoomDatabase

//@Database(entities = [User::class, Wallet::class, Category::class, Payment::class, Stock::class, SavingsGoal::class], version = 1)
//TODO sure?
//@JvmSuppressWildcards
@Database(entities = [User::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}
