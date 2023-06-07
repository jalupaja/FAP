package com.example.fap.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.fap.data.entities.SavingsGoal

@Dao
interface FapDaoSavingGoal {

    @Query("SELECT * FROM SavingsGoal WHERE userId = :userId")
    suspend fun getSavingsGoals(userId: Int): List<SavingsGoal>

    @Insert
    suspend fun insertSavingsGoal(savingsGoal: SavingsGoal): Long
}