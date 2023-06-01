package com.example.fap.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "User")
data class User(
    @PrimaryKey val id: String,
    val name: String? = null,
)

@Entity(tableName = "Wallet",
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["id"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE,
    )]
)
data class Wallet(
    @PrimaryKey
    val name: String,
    val userId: String,
)

@Entity(tableName = "Category",
)
data class Category(
    @PrimaryKey
    val name: String,
)

@Entity(
    tableName = "Payment",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = Wallet::class,
            parentColumns = ["name"],
            childColumns = ["wallet"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = Category::class,
            parentColumns = ["name"],
            childColumns = ["category"],
            onDelete = ForeignKey.CASCADE,
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
)

@Entity(
    tableName = "Stock",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = Wallet::class,
            parentColumns = ["name"],
            childColumns = ["wallet"],
            onDelete = ForeignKey.CASCADE,
        )
    ]
)
data class Stock(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: String,
    val wallet: String,
    val name: String,
    val amount: Int,
    val price: Double,
)

@Entity(
    tableName = "SavingsGoal",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = Wallet::class,
            parentColumns = ["name"],
            childColumns = ["wallet"],
            onDelete = ForeignKey.CASCADE,
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