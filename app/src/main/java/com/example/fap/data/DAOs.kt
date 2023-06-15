package com.example.fap.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import java.util.Date

@Dao
interface FapDao {
    // User
    @Insert
    suspend fun insertUser(user: User)

    @Query("SELECT * FROM user")
    suspend fun getUsers(): List<User>

    // Wallet
    @Query("SELECT * FROM Wallet WHERE userId = :userId")
    suspend fun getWallets(userId: String): List<Wallet>

    @Insert
    suspend fun insertWallet(wallet: Wallet)

    // Payment
    @Upsert
    suspend fun upsertPayment(payment: Payment)

    @Update
    suspend fun updatePayment(payment: Payment)

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

    @Query("SELECT SUM(price) FROM Payment WHERE userId = :userId AND isPayment = 1")
    suspend fun getTotalAmountSpent(userId: String): Double?

    @Query("SELECT SUM(price) FROM Payment WHERE userId = :userId AND isPayment = 1 AND wallet = :wallet")
    suspend fun getTotalAmountSpentByWallet(userId: String, wallet: String): Double?

    @Query("SELECT SUM(price) FROM Payment WHERE userId = :userId AND category = :category AND isPayment = 1")
    suspend fun getTotalAmountSpentByCategory(userId: String, category: String): Double?

    @Query("SELECT SUM(price) FROM Payment WHERE userId = :userId AND isPayment = 0")
    suspend fun getTotalIncome(userId: String): Double?

    @Query("SELECT * FROM payment WHERE userId = :userId AND isPayment = 0 AND date >= :startDate AND date <= :endDate")
    suspend fun getIncomeInRange(userId: String, startDate: Date, endDate: Date): List<Payment>

    @Query("SELECT * FROM payment WHERE userId = :userId AND isPayment = 1 AND date >= :startDate AND date <= :endDate")
    suspend fun getAmountSpentInRange(userId: String, startDate: Date, endDate: Date): List<Payment>

    @Query("SELECT * FROM payment WHERE userId = :userId AND isPayment = 1 AND date >= :startDate AND date <= :endDate AND category = :category")
    suspend fun getAmountSpentInRangeByCategory(userId: String, startDate: Date, endDate: Date, category: String): List<Payment>

    // Category
    @Query("SELECT * FROM Category")
    suspend fun getCategories(): List<Category>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategory(category: Category)

    // SavingsGoal
    @Query("SELECT * FROM SavingsGoal WHERE userId = :userId")
    suspend fun getSavingsGoals(userId: String): List<SavingsGoal>

    @Insert
    suspend fun insertSavingsGoal(savingsGoal: SavingsGoal)
}

@Dao
interface CurrencyDao {
    @Insert
    suspend fun insertCurrency(currency: Currency)

    @Update
    suspend fun updateCurrency(currency: Currency)

    @Query("SELECT conversion FROM Currency WHERE code = :currency")
    suspend fun getConversion(currency: String): Double

}