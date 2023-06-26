package com.example.fap.ui.home

data class WalletInfo(
    val walletName: String,
    val income: Double,
    val expense: Double,
    val incomeMonth: Double,
    val expenseMonth: Double,
    val currency: String,
)