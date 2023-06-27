package com.example.fap.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.fap.data.entities.SavingsGoal

@Dao
interface FapDaoSavingGoal {

    @Query("SELECT * FROM SavingsGoal WHERE userId = :userId")
    suspend fun getSavingsGoals(userId: String): List<SavingsGoal>

    @Insert
    suspend fun insertSavingsGoal(savingsGoal: SavingsGoal): Long

    @Update
    suspend fun updateSavingsGoal(savingsGoal: SavingsGoal)

    @Delete
    suspend fun deleteSavingsGoal(savingsGoal: SavingsGoal)
}
