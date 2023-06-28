package com.example.fap.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.fap.utils.SharedSavingsGoalManager
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
        ),
        ForeignKey(
            entity = Category::class,
            parentColumns = ["name"],
            childColumns = ["category"],
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
    val nextDate: Date,
    val endDate: Date?,
    val timeSpanPerTime: SharedSavingsGoalManager.TimeSpan,
    val amountPerTime: Double,
    val endAmount: Double?,
    val startAmount: Double?,
    val category: String?,
    val isPayment: Boolean,
)