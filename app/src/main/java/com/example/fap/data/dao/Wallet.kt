package com.example.fap.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.fap.data.entities.Wallet

@Dao
interface FapDaoWallet {

    @Query("SELECT * FROM Wallet WHERE userId = :userId")
    suspend fun getWallets(userId: Int): List<Wallet>

    @Insert
    suspend fun insertWallet(wallet: Wallet): Long

}