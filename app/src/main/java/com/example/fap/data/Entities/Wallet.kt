package com.example.fap.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.fap.data.entities.User

@Entity(tableName = "Wallet",
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["id"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Wallet(
    @PrimaryKey
    val name: String,
    val userId: String,
)
