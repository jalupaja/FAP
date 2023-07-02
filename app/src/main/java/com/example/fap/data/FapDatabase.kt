package com.example.fap.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.fap.R
import com.example.fap.data.dao.FapDaoCategory
import com.example.fap.data.dao.FapDaoPayment
import com.example.fap.data.dao.FapDaoSavingGoal
import com.example.fap.data.dao.FapDaoUser
import com.example.fap.data.dao.FapDaoWallet
import com.example.fap.data.dao.FapDaoCurrency
import com.example.fap.data.entities.Category
import com.example.fap.data.entities.Currency
import com.example.fap.data.entities.Payment
import com.example.fap.data.entities.SavingsGoal
import com.example.fap.data.entities.Stock
import com.example.fap.data.entities.User
import com.example.fap.data.entities.Wallet
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory

@Database(
    entities = [
        User::class,
        Wallet::class,
        Category::class,
        Payment::class,
        Stock::class,
        SavingsGoal::class,
        Currency::class,
    ],
    version = 1
)

@TypeConverters(Converters::class)
abstract class FapDatabase: RoomDatabase() {
    abstract fun fapDaoCategory(): FapDaoCategory
    abstract fun fapDaoPayment(): FapDaoPayment
    abstract fun fapDaoSavingGoal(): FapDaoSavingGoal
    abstract fun fapDaoUser(): FapDaoUser
    abstract fun fapDaoWallet(): FapDaoWallet
    abstract fun fapDaoCurrency(): FapDaoCurrency

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
                .openHelperFactory(factory) /* use password */
                .build()
            }
        }
    }
}

