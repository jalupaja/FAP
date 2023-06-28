package com.example.fap.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

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
            parentColumns = ["name"],
            childColumns = ["wallet"],
            onDelete = ForeignKey.CASCADE
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
