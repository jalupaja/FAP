package com.example.fap.ui.history

data class HistoryItem(
    val id: Int,
    val title: String,
    val category: String,
    val price: Double,
    val isPayment: Boolean,
)