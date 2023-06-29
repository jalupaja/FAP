package com.example.fap.ui.dialogs

import android.app.AlertDialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.fap.data.FapDatabase
import com.example.fap.databinding.DialogAddPaymentBinding
import com.example.fap.utils.SharedPreferencesManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import android.app.DatePickerDialog
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.OnBackPressedCallback
import com.example.fap.R
import com.example.fap.data.entities.Category
import com.example.fap.data.entities.Payment
import com.example.fap.data.entities.SavingsGoal
import com.example.fap.utils.SharedCurrencyManager
import com.example.fap.utils.SharedSavingsGoalManager
import com.google.android.material.snackbar.Snackbar
import java.time.ZoneId
import java.util.*

class AddPayment : AppCompatActivity() {

    private lateinit var binding: DialogAddPaymentBinding
    private lateinit var currencyAdapter: ArrayAdapter<String>
    private lateinit var walletAdapter: ArrayAdapter<String>
    private lateinit var categoryAdapter: ArrayAdapter<String>
    private var isPayment: Boolean = true
    private lateinit var sharedPreferences: SharedPreferencesManager
    private lateinit var sharedCurrency: SharedCurrencyManager
    private lateinit var sharedSavingsGoal: SharedSavingsGoalManager
    private var curItemId = -1
    private var curSavingsGoalId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DialogAddPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        sharedPreferences = SharedPreferencesManager.getInstance(applicationContext)
        sharedCurrency = SharedCurrencyManager.getInstance(applicationContext)
        sharedSavingsGoal = SharedSavingsGoalManager.getInstance(applicationContext)

        val dbPayment = FapDatabase.getInstance(applicationContext).fapDaoPayment()
        val dbWallet = FapDatabase.getInstance(applicationContext).fapDaoWallet()
        val dbCategory = FapDatabase.getInstance(applicationContext).fapDaoCategory()
        val dbSavingsGoal = FapDatabase.getInstance(applicationContext).fapDaoSavingGoal()
        val curUser = sharedPreferences.getCurUser(applicationContext)
        var previousReptition = SharedSavingsGoalManager.TimeSpan.None
        val dateFormatPattern = "dd.MM.yyyy"
        val repetitionPrefix = "Repetition: "

        val btnBack = binding.btnBack
        val btnDel = binding.btnDel
        val itemTitle = binding.titleInput
        val itemDate = binding.datePicker
        val itemPrice = binding.priceInput
        val itemCurrency = binding.currencySpinner
        val itemCategory = binding.categorySpinner
        val itemWallet = binding.walletSpinner
        val btnIsPayment = binding.btnIsPayment
        val itemRepetition = binding.repetitionPicker
        val btnIsIncome = binding.btnIsIncome
        val itemDescription = binding.descriptionInput
        val btnSave = binding.btnSave

        // onClickListeners
        btnBack.setOnClickListener {
            backButtonCallback.handleOnBackPressed()
        }
        btnDel.setOnClickListener {
            if (curSavingsGoalId == null) {
                val alert = AlertDialog.Builder(this)
                alert.setMessage("Are you sure you want to delete this item?")
                alert.setTitle("Confirmation")
                alert.setPositiveButton("Yes") { dialog, _ ->
                    lifecycleScope.launch {
                        dbPayment.deletePayment(curItemId)
                    }
                    dialog.dismiss()
                    backButtonCallback.handleOnBackPressed()
                }
                alert.setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                alert.show()
            } else {
                val options = arrayOf("Delete the selected occurrence only", "Delete this and all future occurrences", "Delete all occurrences")

                val alert = AlertDialog.Builder(this@AddPayment)
                alert.setItems(options) { dialog, selected ->
                    when (selected) {
                        0 -> {
                            lifecycleScope.launch {
                                dbPayment.deletePayment(curItemId)
                            }
                        }
                        1 -> {
                            lifecycleScope.launch {
                                // TODO test (if wrong also change in change!)
                                dbPayment.removeSavingsGoalIdBeforePayment(curSavingsGoalId!!, curItemId)
                                dbSavingsGoal.deleteSavingsGoalById(curSavingsGoalId!!)
                            }
                        }
                        2 -> {
                            lifecycleScope.launch {
                                dbSavingsGoal.deleteSavingsGoalById(curSavingsGoalId!!)
                            }
                        }
                        else -> { }
                    }
                    dialog.dismiss()
                    backButtonCallback.handleOnBackPressed()
                }
                alert.setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }

                alert.show()
            }
        }

        btnIsPayment.setOnClickListener {
            btnIsPayment.alpha = 1F
            btnIsIncome.alpha = 0.7F
            isPayment = true
        }

        btnIsIncome.setOnClickListener {
            btnIsPayment.alpha = 0.7F
            btnIsIncome.alpha = 1F
            isPayment = false
        }

        itemDate.setOnClickListener {
            val curDate = LocalDate.parse(itemDate.text.toString(), DateTimeFormatter.ofPattern(dateFormatPattern))
            val datePickerDialog = DatePickerDialog(this@AddPayment, { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(Calendar.YEAR, year)
                selectedDate.set(Calendar.MONTH, month - 1)
                selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val formattedDate = SimpleDateFormat(dateFormatPattern, Locale.getDefault()).format(selectedDate.time)

                itemDate.setText(formattedDate)
            }, curDate.year, curDate.monthValue, curDate.dayOfMonth)

            datePickerDialog.show()
        }

        itemRepetition.setOnClickListener {
            val options = SharedSavingsGoalManager.TimeSpan.values().map { it.label }.toTypedArray()

            val alert = AlertDialog.Builder(this@AddPayment)
            alert.setItems(options) { dialog, selected ->
                val selectedOption = SharedSavingsGoalManager.TimeSpan.values()[selected]
                itemRepetition.setText(repetitionPrefix + selectedOption.label)
                dialog.dismiss()
            }
            alert.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

            alert.show()
        }

        btnSave.setOnClickListener {
            val wallet = itemWallet.selectedItem?.toString() ?: ""
            val title = itemTitle.text?.toString() ?: ""
            val price = itemPrice.text?.toString() ?: ""
            val currency = itemCurrency.selectedItem?.toString() ?: ""
            val description = itemDescription.text?.toString() ?: ""
            val date = Date.from(
                LocalDate.parse(
                    itemDate.text.toString(),
                    DateTimeFormatter.ofPattern(dateFormatPattern)
                ).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()
            )
            val category = itemCategory.text?.toString() ?: ""
            val repetition = SharedSavingsGoalManager.TimeSpan.valueOf(
                itemRepetition.text?.toString()?.removePrefix(repetitionPrefix) ?: SharedSavingsGoalManager.TimeSpan.None.label
            )
            val endDate = null
            val startAmount = 0.0
            val endAmount = 0.0

            // check if important fields are filled
            if (title.isEmpty() || price.isEmpty()) {
                Snackbar.make(
                    binding.root,
                    "Please fill the title and price",
                    Snackbar.LENGTH_SHORT
                ).show()
            } else {
                lifecycleScope.launch {
                    var newPayment = Payment(
                        userId = curUser,
                        wallet = wallet,
                        title = title,
                        description = description,
                        price = sharedCurrency.calculateFromCurrency(
                            price.toDouble(),
                            currency,
                            applicationContext
                        ),
                        date = date,
                        isPayment = isPayment,
                        category = category,
                        savingsGoalId = curSavingsGoalId,
                    )
                    if (curItemId != -1) {
                        newPayment = newPayment.copy(id = curItemId)
                    }

                    val newTitle = sharedSavingsGoal.reTimSpanTitle(title, repetition)
                    var newRepeatingPayment = SavingsGoal(
                        userId = curUser,
                        wallet = wallet,
                        title = newTitle,
                        description = description,
                        nextDate = date,
                        timeSpanPerTime = repetition,
                        amountPerTime = sharedCurrency.calculateFromCurrency(
                            price.toDouble(),
                            currency,
                            applicationContext
                        ),
                        isPayment = isPayment,
                        category = category,
                        endDate = endDate,
                        startAmount = startAmount,
                        endAmount = endAmount,
                    )
                    if (curSavingsGoalId != null) {
                        newRepeatingPayment = newRepeatingPayment.copy(id = curSavingsGoalId!!)
                    }

                    dbCategory.insertCategory(Category(category))

                    if (previousReptition == SharedSavingsGoalManager.TimeSpan.None && repetition == SharedSavingsGoalManager.TimeSpan.None) {
                        /* is and never was a repeating payment */
                        dbPayment.upsertPayment(newPayment)
                        backButtonCallback.handleOnBackPressed()
                    } else if (previousReptition == SharedSavingsGoalManager.TimeSpan.None && repetition != SharedSavingsGoalManager.TimeSpan.None) {
                        /* it is now a repeating payment */
                        dbCategory.insertCategory(Category(category))
                        dbSavingsGoal.insertSavingsGoal(newRepeatingPayment).toInt()

                        sharedSavingsGoal.updateSavingsGoals(applicationContext)
                        backButtonCallback.handleOnBackPressed()
                    } else if (previousReptition != SharedSavingsGoalManager.TimeSpan.None && repetition == SharedSavingsGoalManager.TimeSpan.None) {
                        /* it was a repeating payment */
                        val alert = AlertDialog.Builder(applicationContext)
                        alert.setMessage("This will remove this Payment from the repetition but continue the repetition.")
                        alert.setTitle("Confirmation")
                        alert.setPositiveButton("Yes") { dialog, _ ->
                            lifecycleScope.launch {
                                dbPayment.upsertPayment(
                                    newPayment.copy(savingsGoalId = null)
                                )
                                backButtonCallback.handleOnBackPressed()
                            }
                            dialog.dismiss()
                        }
                        alert.setNegativeButton("No") { dialog, _ ->
                            dialog.dismiss()
                        }
                        alert.show()
                    } else if (previousReptition != SharedSavingsGoalManager.TimeSpan.None && previousReptition == repetition) {
                        /* this was a repeating payment is staying the same */
                        val options = arrayOf("Change the selected occurrence only", "Change this and all future occurrences", "Change all occurrences")

                        val alert = AlertDialog.Builder(this@AddPayment)
                        alert.setItems(options) { dialog, selected ->
                            when (selected) {
                                0 -> {
                                    lifecycleScope.launch {
                                        dbPayment.upsertPayment(newPayment.copy(title = newTitle))
                                    }
                                }

                                1 -> {
                                    lifecycleScope.launch {
                                        // TODO test (if wrong also change in delete!)
                                        dbPayment.removeSavingsGoalIdBeforePayment(
                                            curSavingsGoalId!!,
                                            curItemId
                                        )
                                        // TODO test
                                        dbPayment.updatePaymentsBySavingsGoalFromPayment(newPayment.wallet, newTitle, newPayment.description!!, newPayment.price, newPayment.isPayment, newPayment.category!!, curSavingsGoalId!!, curItemId)
                                        dbSavingsGoal.updateSavingsGoal(newRepeatingPayment)
                                    }
                                }

                                2 -> {
                                    lifecycleScope.launch {
                                        //TODO test
                                        dbPayment.updatePaymentsBySavingsGoal(newPayment.wallet, newTitle, newPayment.description!!, newPayment.price, newPayment.isPayment, newPayment.category!!, curSavingsGoalId!!)
                                        dbSavingsGoal.updateSavingsGoal(newRepeatingPayment)
                                    }
                                }

                                else -> {}
                            }
                            dialog.dismiss()
                            backButtonCallback.handleOnBackPressed()
                        }
                        alert.setNegativeButton("Cancel") { dialog, _ ->
                            dialog.dismiss()
                        }

                        alert.show()
                    } else if (previousReptition != SharedSavingsGoalManager.TimeSpan.None && previousReptition != repetition) {
                        /* this was a repeating payment but repetition changed */
                        // TODO fix title
                        // TODO if change: tell user: this will delete all after
                        // if not changed: upsert thing, add SavingsID!!!
                        val alert = AlertDialog.Builder(applicationContext)
                        alert.setMessage("This will remove this and all following payments from the previous repetition and create a new repetition.")
                        alert.setTitle("Confirmation")
                        alert.setPositiveButton("Yes") { dialog, _ ->
                            lifecycleScope.launch {
                                dbPayment.removeSavingsGoalIdBeforePayment(curSavingsGoalId!!, curItemId)
                                dbSavingsGoal.deleteSavingsGoalById(curSavingsGoalId!!)

                                dbCategory.insertCategory(Category(category))
                                dbSavingsGoal.insertSavingsGoal(newRepeatingPayment).toInt()

                                sharedSavingsGoal.updateSavingsGoals(applicationContext)
                                backButtonCallback.handleOnBackPressed()
                            }
                            dialog.dismiss()
                        }
                        alert.setNegativeButton("No") { dialog, _ ->
                            dialog.dismiss()
                        }
                        alert.show()
                    }
                }
            }
            categoryAdapter.notifyDataSetChanged()
        }

    curItemId = intent.getIntExtra("paymentId", -1)

    currencyAdapter = ArrayAdapter(applicationContext, R.layout.spinner_item)
    walletAdapter = ArrayAdapter(applicationContext, R.layout.spinner_item)
    val categorySpinner = binding.categorySpinner

    var startTitle = ""
    var startDate = SimpleDateFormat(dateFormatPattern, Locale.getDefault()).format(Date())
    var startPrice = ""
    val startCurrency = sharedCurrency.getDefaultCurrencyIndex()
    var startCategory = ""
    var startWallet = 0
    var startRepetition = SharedSavingsGoalManager.TimeSpan.None.label
    var startDescription = ""

    lifecycleScope.launch {
        // Setup existing wallets
        currencyAdapter.addAll(sharedCurrency.getAvailableCurrencies())
        itemCurrency.adapter = currencyAdapter
        val wallets = dbWallet.getWallets(curUser)
        itemWallet.adapter = walletAdapter
        for (wallet in wallets) {
            walletAdapter.add(wallet.name)
        }

        // Setup existing categories
        val categories = dbCategory.getCategories()
        categoryAdapter = ArrayAdapter<String>(applicationContext, R.layout.spinner_item)
        categorySpinner.setAdapter(categoryAdapter)
        for (category in categories) {
            categoryAdapter.add(category.name)
        }

        // Update default values if this is supposed to edit an existing item
        if (curItemId != -1) {
            btnDel.visibility = View.VISIBLE
            val item = dbPayment.getPayment(curItemId)
            startTitle = item.title
            startDate = SimpleDateFormat(dateFormatPattern, Locale.getDefault()).format(item.date)
            startPrice = "%.2f".format(item.price)
            startCategory = item.category ?: ""
            startWallet = walletAdapter.getPosition(item.wallet)
            isPayment = item.isPayment
            curSavingsGoalId = item.savingsGoalId
            if (curSavingsGoalId != null) {
                previousReptition = dbSavingsGoal.getTimeSpan(curSavingsGoalId!!)
                startRepetition = previousReptition.label
            }
            startDescription = item.description ?: ""
        }

        // Setup default values
        itemTitle.setText(startTitle)
        itemDate.setText(startDate)
        itemPrice.setText(startPrice)
        itemCurrency.setSelection(startCurrency)
        itemCategory.setText(startCategory)
        itemWallet.setSelection(startWallet)
        if (isPayment) {
            btnIsPayment.callOnClick()
        } else {
            btnIsIncome.callOnClick()
        }
        itemRepetition.setText(repetitionPrefix + startRepetition)
        itemDescription.setText(startDescription)
    }

    onBackPressedDispatcher.addCallback(this, backButtonCallback)
}

private val backButtonCallback = object : OnBackPressedCallback(true) {
    override fun handleOnBackPressed() {
        finish()
    }
}

override fun onOptionsItemSelected(item: MenuItem): Boolean {
    // Fix Back Button in Toolbar
    if (item.itemId == android.R.id.home) {
        backButtonCallback.handleOnBackPressed()
        return true
    }
    return super.onOptionsItemSelected(item)
}

}
