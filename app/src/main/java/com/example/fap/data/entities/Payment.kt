package com.example.fap.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "Payment",
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
        ),
        ForeignKey(
            entity = SavingsGoal::class,
            parentColumns = ["id"],
            childColumns = ["savingsGoalId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Payment(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: String,
    val wallet: String,
    val title: String,
    val description: String?,
    val price: Double,
    val date: Date,
    val isPayment: Boolean,
    val category: String?,
    val savingsGoalId: Int? = null,
)
