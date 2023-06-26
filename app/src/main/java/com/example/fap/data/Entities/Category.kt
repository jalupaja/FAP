package com.example.fap.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "Category",
    foreignKeys = [ForeignKey(
        entity = User::class,
        onDelete = ForeignKey.CASCADE
    )]
)
data class Category(
    @PrimaryKey
    val name: String,
)
