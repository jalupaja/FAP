package com.example.fap.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.fap.data.entities.Category

@Dao
interface FapDaoCategory {

    @Query("SELECT * FROM Category WHERE userId = :userId")
    suspend fun getCategories(userId: Int): List<Category>

    @Insert
    suspend fun insertCategory(category: Category): Long
}