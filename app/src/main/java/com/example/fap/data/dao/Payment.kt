package com.example.fap.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.fap.data.entities.Payment
import java.util.Date

@Dao
interface FapDaoPayment {

    @Insert
    suspend fun insertPayment(payment: Payment)

    @Update
    suspend fun updatePayment(payment: Payment)

    @Delete
    suspend fun deletePayment(payment: Payment)

    @Query("SELECT * FROM Payment WHERE userId = :userId")
    suspend fun getPayments(userId: String): List<Payment>

    @Query("SELECT * FROM Payment WHERE userId = :userId AND walletId = :walletId")
    suspend fun getPaymentsByWallet(userId: String, walletId: Int): List<Payment>

    @Query("SELECT SUM(price) FROM Payment WHERE userId = :userId AND isPayment = 1")
    suspend fun getTotalAmountSpent(userId: String): Double

    @Query("SELECT SUM(price) FROM Payment WHERE userId = :userId AND isPayment = 1 AND walletId = :walletId")
    suspend fun getTotalAmountSpentByWallet(userId: String, walletId: Int): Double

    @Query("SELECT SUM(price) FROM Payment WHERE userId = :userId AND categoryId = :categoryId AND isPayment = 1")
    suspend fun getTotalAmountSpentByCategory(userId: String, categoryId: Int): Double

    @Query("SELECT SUM(price) FROM Payment WHERE userId = :userId AND isPayment = 0")
    suspend fun getTotalIncome(userId: String): Double

    @Query("SELECT * FROM payment WHERE userId = :userId AND isPayment = 0 AND date >= :startDate AND date <= :endDate")
    suspend fun getIncomeInRange(userId: String, startDate: Date, endDate: Date): List<Payment>

    @Query("SELECT * FROM payment WHERE userId = :userId AND isPayment = 1 AND date >= :startDate AND date <= :endDate")
    suspend fun getAmountSpentInRange(userId: String, startDate: Date, endDate: Date): List<Payment>

    @Query("SELECT * FROM payment WHERE userId = :userId AND isPayment = 1 AND date >= :startDate AND date <= :endDate AND categoryId = :categoryId")
    suspend fun getAmountSpentInRangeByCategory(userId: String, startDate: Date, endDate: Date, categoryId: Int): List<Payment>

}