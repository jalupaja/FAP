package com.example.fap.ui.home

import com.example.fap.ui.category.CategoryItem

data class WalletInfo(
    val walletName: String,
    val income: Double,
    val expense: Double,
    val incomeMonth: Double,
    val expenseMonth: Double,
    val currency: String,
    val category: List<CategoryItem>
)