package com.example.fap.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Currency(
    @PrimaryKey
    val code: String,
    val conversion: Double,
)
