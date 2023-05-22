package com.example.fap.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

// TODO double check everything here

@Entity(tableName = "User")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String
)
/*
@Entity(tableName = "Wallet")
data class Wallet(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val name: String
)

@Entity(tableName = "Category")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
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
    val userId: Int,
    val walletId: Int,
    val title: String,
    val description: String,
    val price: Double,
    val date: String,
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
    val userId: Int,
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
    val userId: Int,
    val walletId: Int,
    val title: String,
    val description: String,
    val startDate: String,
    val endDate: String,
    val amountPerMonth: Double,
    val endAmount: Double,
    val startAmount: Double
)
*/