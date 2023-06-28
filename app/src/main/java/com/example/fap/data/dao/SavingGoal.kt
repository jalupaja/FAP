package com.example.fap.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.fap.data.entities.SavingsGoal
import com.example.fap.utils.SharedSavingsGoalManager

@Dao
interface FapDaoSavingGoal {

    @Query("SELECT * FROM SavingsGoal WHERE userId = :userId")
    suspend fun getSavingsGoals(userId: String): List<SavingsGoal>

    @Query("SELECT timeSpanPerTime FROM SavingsGoal WHERE id = :id")
    suspend fun getTimeSpan(id: Int): SharedSavingsGoalManager.TimeSpan

    @Insert
    suspend fun insertSavingsGoal(savingsGoal: SavingsGoal): Long

    @Update
    suspend fun updateSavingsGoal(savingsGoal: SavingsGoal)

    @Delete
    suspend fun deleteSavingsGoal(savingsGoal: SavingsGoal)
}
