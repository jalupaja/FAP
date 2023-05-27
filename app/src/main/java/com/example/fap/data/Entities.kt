package com.example.fap.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "User")
data class User(
    @PrimaryKey val id: String,
    val name: String? = null
)

@Entity(tableName = "Wallet",
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["id"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Wallet(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: String,
    val name: String
)

@Entity(tableName = "Category",
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["id"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: String,
    val name: String
)

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
            parentColumns = ["id"],
            childColumns = ["walletId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Payment(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: String,
    val walletId: Int,
    val title: String,
    val description: String,
    val price: Double,
    val date: Date,
    val isPayment: Boolean,
    val categoryId: Int
)

@Entity(
    tableName = "Stock",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Wallet::class,
            parentColumns = ["id"],
            childColumns = ["walletId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Stock(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: String,
    val walletId: Int,
    val name: String,
    val amount: Int,
    val price: Double
)

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
            parentColumns = ["id"],
            childColumns = ["walletId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class SavingsGoal(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: String,
    val walletId: Int,
    val title: String,
    val description: String,
    val startDate: Date,
    val endDate: Date,
    val amountPerMonth: Double,
    val endAmount: Double,
    val startAmount: Double
)