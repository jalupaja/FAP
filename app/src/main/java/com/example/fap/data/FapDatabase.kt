package com.example.fap.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.fap.R
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory

@Database(
    entities = [
        User::class,
        Wallet::class,
        Category::class,
        Payment::class,
        Stock::class,
        SavingsGoal::class
               ],
    version = 1
)
@TypeConverters(Converters::class)
abstract class FapDatabase: RoomDatabase() {
    abstract fun fapDao(): FapDao

    companion object {
        private var instance: FapDatabase? = null
        private lateinit var factory: SupportFactory

        fun getInstance(context: Context, password: String = ""): FapDatabase {
            return instance ?: synchronized(this) {
                if (password.isNotEmpty()) {
                    factory = SupportFactory(SQLiteDatabase.getBytes(password.toCharArray()), null, false)
                }
                return Room.databaseBuilder(
                    context.applicationContext,
                    FapDatabase::class.java,
                    context.getString(R.string.database_name)
                )
                //.openHelperFactory(factory) /* use password */
                .build()
            }
        }
    }
}

