package com.example.fap.ui.history

import java.util.Date

data class HistoryItem(
    val id: Int,
    val title: String,
    val category: String,
    val price: Double,
    val isPayment: Boolean,
    val date: Date
)