package com.example.fap.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "SavingsGoal",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Wallet::class,
            parentColumns = ["name"],
            childColumns = ["wallet"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)

data class SavingsGoal(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: String,
    val wallet: String,
    val title: String,
    val description: String,
    val startDate: Date,
    val endDate: Date,
    val amountPerMonth: Double,
    val endAmount: Double,
    val startAmount: Double,
)
