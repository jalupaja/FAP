package com.example.fap.data

import androidx.room.Embedded
import androidx.room.Relation
import com.example.fap.data.entities.Payment
import com.example.fap.data.entities.Wallet

data class PaymentsByWallets(
    @Embedded
    val wallet: Wallet,
    @Relation(
        parentColumn = "name",
        entityColumn = "wallet"
    )
    val payments: List<Payment>
)
