package com.example.fap.data.entities

import androidx.room.Embedded
import androidx.room.Relation

data class PaymentsByWallets(
    @Embedded
    val wallet: Wallet,
    @Relation(
        parentColumn = "name",
        entityColumn = "wallet"
    )
    val payments: List<Payment>
)
