package com.example.fap.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.fap.data.entities.Currency

@Dao
interface CurrencyDao {
    @Insert
    suspend fun insertCurrency(currency: Currency)

    @Update
    suspend fun updateCurrency(currency: Currency)

    @Query("SELECT conversion FROM Currency WHERE code = :currency")
    suspend fun getConversion(currency: String): Double
}
