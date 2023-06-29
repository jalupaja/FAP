package com.example.fap.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.fap.data.entities.Payment
import java.util.Date
import androidx.room.Upsert
import com.example.fap.data.entities.PaymentsByWallets

@Dao
interface FapDaoPayment {

    @Insert
    suspend fun insertPayment(payment: Payment)

    @Upsert
    suspend fun upsertPayment(payment: Payment)

    @Update
    suspend fun updatePayments(payments: List<Payment>)

    @Query("DELETE FROM Payment WHERE id = :id")
    suspend fun deletePayment(id: Int)

    @Query("SELECT * FROM Payment WHERE userId = :userId")
    suspend fun getPayments(userId: String): List<Payment>

    @Query("SELECT * FROM Payment WHERE id = :itemId")
    suspend fun getPayment(itemId: Int): Payment

    @Query("SELECT * FROM Payment WHERE userId = :userId AND wallet = :wallet")
    suspend fun getPaymentsByWallet(userId: String, wallet: String): List<Payment>

    @Query("SELECT * FROM Payment WHERE userId = :userId AND category = :category")
    suspend fun getPaymentsByCategory(userId: String, category: String): List<Payment>

    @Query("SELECT SUM(price) FROM Payment WHERE userId = :userId AND isPayment = 1")
    suspend fun getTotalAmountSpent(userId: String): Double?

    @Query("SELECT SUM(price) FROM Payment WHERE userId = :userId AND isPayment = 1 AND wallet = :wallet")
    suspend fun getTotalAmountSpentByWallet(userId: String, wallet: String): Double?

    @Query("SELECT SUM(price) FROM Payment WHERE userId = :userId AND category = :category AND isPayment = 1")
    suspend fun getTotalAmountSpentByCategory(userId: String, category: String): Double?

    @Query("SELECT SUM(CASE WHEN p.isPayment = 1 THEN -p.price ELSE p.price END) AS sumCat FROM Payment p WHERE p.userId = :userId AND p.category = :category")
    suspend fun getTotalAmountByCategory(userId: String, category: String): Double?

    @Query("SELECT SUM(price) FROM Payment WHERE userId = :userId AND isPayment = 0")
    suspend fun getTotalIncome(userId: String): Double?

    @Query("SELECT * FROM payment WHERE userId = :userId AND isPayment = 0 AND date >= :startDate AND date <= :endDate")
    suspend fun getIncomeInRange(userId: String, startDate: Date, endDate: Date): List<Payment>

    @Query("SELECT * FROM payment WHERE userId = :userId AND isPayment = 1 AND date >= :startDate AND date <= :endDate")
    suspend fun getAmountSpentInRange(userId: String, startDate: Date, endDate: Date): List<Payment>

    @Query("SELECT * FROM payment WHERE userId = :userId AND isPayment = 1 AND date >= :startDate AND date <= :endDate AND category = :category")
    suspend fun getAmountSpentInRangeByCategory(userId: String, startDate: Date, endDate: Date, category: String): List<Payment>

    @Query("SELECT * FROM Wallet WHERE userId = :userId")
    suspend fun getPaymentsByWallets(userId: String): List<PaymentsByWallets>

    @Query("UPDATE Payment set savingsGoalId = null WHERE savingsGoalId = :savingsGoalId AND date < (SELECT date FROM Payment WHERE id = :paymentId)")
    suspend fun removeSavingsGoalIdBeforePayment(savingsGoalId: Int, paymentId: Int)

    @Query("UPDATE Payment set wallet = :wallet AND title = :title AND description = :description AND price = :price AND isPayment = :isPayment AND category = :category WHERE savingsGoalId = :savingsGoalId")
    suspend fun updatePaymentsBySavingsGoal(wallet: String, title: String, description: String, price: Double, isPayment: Boolean, category: String, savingsGoalId: Int)

    @Query("UPDATE Payment set wallet = :wallet AND title = :title AND description = :description AND price = :price AND isPayment = :isPayment AND category = :category WHERE savingsGoalId = :savingsGoalId AND date >= (SELECT date FROM Payment WHERE id = :curPaymentId)")
    suspend fun updatePaymentsBySavingsGoalFromPayment(wallet: String, title: String, description: String, price: Double, isPayment: Boolean, category: String, savingsGoalId: Int, curPaymentId: Int)
}
