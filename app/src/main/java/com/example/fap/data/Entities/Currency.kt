package com.example.fap.data.entities

import androidx.room.Entity

@Entity
data class Currency(
    @PrimaryKey
    val code: String,
    val conversion: Double,
)
