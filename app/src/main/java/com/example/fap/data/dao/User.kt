package com.example.fap.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.fap.data.entities.User

@Dao
interface FapDaoUser {

    @Insert
    suspend fun insertUser(user: User)

    @Query("SELECT * FROM user")
    suspend fun getUsers(): List<User>

}
