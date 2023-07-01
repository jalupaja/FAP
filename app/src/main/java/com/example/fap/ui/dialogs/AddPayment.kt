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
import android.widget.LinearLayout
import android.widget.RadioButton
import androidx.activity.OnBackPressedCallback
import androidx.core.view.forEachIndexed
import com.example.fap.R
import com.example.fap.data.entities.Category
import com.example.fap.data.entities.Payment
import com.example.fap.data.entities.SavingsGoal
import com.example.fap.databinding.DialogRadioButtonsBinding
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
        var previousRepetition = SharedSavingsGoalManager.TimeSpan.None
        val dateFormatPattern = "dd.MM.yyyy"
        var repetitionPrefix = "Repetition: "
        var isSavingsGoal = false

        val btnBack = binding.btnBack
        val btnDel = binding.btnDel
        val itemTitle = binding.titleInput
        val itemDateStartLayout = binding.datePickerStartLayout
        val itemDateStart = binding.datePickerStart
        val itemDateEndLayout = binding.datePickerEndLayout
        val itemDateEnd = binding.datePickerEnd
        val itemPrice = binding.priceInput
        val itemCurrency = binding.currencySpinner
        val itemCategory = binding.categorySpinner
        val itemWallet = binding.walletSpinner
        val btnIsPayment = binding.btnIsPayment
        val itemSavingsGoal = binding.savingsGoalPicker
        val itemSavingsGoalLayout = binding.savingsGoalPickerLayout
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
                val dialogBinding = DialogRadioButtonsBinding.inflate(layoutInflater)
                val alert = AlertDialog.Builder(this@AddPayment)
                alert.setView(dialogBinding.root)
                val btnGroup = dialogBinding.btnGroup
                val options = arrayOf("Delete the selected occurrence only", "Delete this and all future occurrences", "Delete all occurrences")

                for (option in options) {
                    val btn = RadioButton(this@AddPayment, )
                    btn.text = option
                    val layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    layoutParams.setMargins(0, 10, 0, 10)
                    btn.layoutParams = layoutParams
                    btnGroup.addView(btn)
                }

                alert.setPositiveButton(("Yes")) { dialog, _ ->
                    var selected = -1
                    btnGroup.forEachIndexed { index, view ->
                        if (view is RadioButton && view.isChecked) {
                            selected = index
                            return@forEachIndexed
                        }
                    }
                    when (selected) {
                        0 -> {
                            lifecycleScope.launch {
                                dbPayment.deletePayment(curItemId)
                                backButtonCallback.handleOnBackPressed()
                            }
                        }
                        1 -> {
                            lifecycleScope.launch {
                                dbPayment.removeSavingsGoalIdBeforePayment(curSavingsGoalId!!, curItemId)
                                dbSavingsGoal.deleteSavingsGoalById(curSavingsGoalId!!)
                                backButtonCallback.handleOnBackPressed()
                            }
                        }
                        2 -> {
                            lifecycleScope.launch {
                                dbSavingsGoal.deleteSavingsGoalById(curSavingsGoalId!!)
                                backButtonCallback.handleOnBackPressed()
                            }
                        }
                        else -> { }
                    }
                    dialog.dismiss()
                }
                alert.setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }

                alert.show()
            }
        }

        itemSavingsGoal.setOnClickListener {
            // TODO placement of the button
            // TODO probably fine: what if this changes on edit?, What if it changes from Repeating to SavingsGoal?
            // TODO probably fine: del, save
            if (isSavingsGoal) {
                isSavingsGoal = false
                itemSavingsGoalLayout.hint = getString(R.string.savingsgoal_false)

                val newRepetitionPrefix = "Repetition: "
                itemRepetition.setText(
                    newRepetitionPrefix + itemRepetition.text?.removePrefix(
                        repetitionPrefix
                    )
                )
                repetitionPrefix = newRepetitionPrefix

                itemDateEnd.setText(
                    SimpleDateFormat(dateFormatPattern, Locale.getDefault()).format(
                        Date(0)
                    )
                )
                itemDateEndLayout.visibility = View.GONE
                itemDateStartLayout.hint = "Date"
            } else {
                isSavingsGoal = true
                itemSavingsGoalLayout.hint = getString(R.string.savingsgoal_true)
                // TODO rename Repetitionprefix
                val newRepetitionPrefix = "per payment: "
                var curRepetition = itemRepetition.text?.removePrefix(repetitionPrefix).toString()

                if (curRepetition == SharedSavingsGoalManager.TimeSpan.None.label) {
                    curRepetition = SharedSavingsGoalManager.TimeSpan.Monthly.label
                }

                itemRepetition.setText(newRepetitionPrefix + curRepetition)
                repetitionPrefix = newRepetitionPrefix

                if (itemDateEnd.text.toString() == SimpleDateFormat(
                        dateFormatPattern,
                        Locale.getDefault()
                    ).format(Date(0))
                ) {
                    val date = Date.from(
                        LocalDate.parse(
                            itemDateStart.text.toString(),
                            DateTimeFormatter.ofPattern(dateFormatPattern)
                        )
                            .plusMonths(6)
                            .atStartOfDay()
                            .atZone(ZoneId.systemDefault())
                            .toInstant()
                    )

                    itemDateEnd.setText(
                        SimpleDateFormat(
                            dateFormatPattern,
                            Locale.getDefault()
                        ).format(date)
                    )
                }
                itemDateEndLayout.visibility = View.VISIBLE
                itemDateStartLayout.hint = "Start Date"
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

        itemDateStart.setOnClickListener {
            val curDate = LocalDate.parse(itemDateStart.text.toString(), DateTimeFormatter.ofPattern(dateFormatPattern))
            val datePickerDialog = DatePickerDialog(this@AddPayment, { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(Calendar.YEAR, year)
                selectedDate.set(Calendar.MONTH, month)
                selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val formattedDate = SimpleDateFormat(dateFormatPattern, Locale.getDefault()).format(selectedDate.time)

                itemDateStart.setText(formattedDate)
            }, curDate.year, curDate.monthValue - 1, curDate.dayOfMonth)

            datePickerDialog.show()
        }

        itemDateEnd.setOnClickListener {
            val curDate = LocalDate.parse(itemDateEnd.text.toString(), DateTimeFormatter.ofPattern(dateFormatPattern))
            val datePickerDialog = DatePickerDialog(this@AddPayment, { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(Calendar.YEAR, year)
                selectedDate.set(Calendar.MONTH, month)
                selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val formattedDate = SimpleDateFormat(dateFormatPattern, Locale.getDefault()).format(selectedDate.time)

                itemDateEnd.setText(formattedDate)
            }, curDate.year, curDate.monthValue - 1, curDate.dayOfMonth)

            datePickerDialog.show()
        }

        itemRepetition.setOnClickListener {

            val dialogBinding = DialogRadioButtonsBinding.inflate(layoutInflater)
            val alert = AlertDialog.Builder(this@AddPayment)
            alert.setView(dialogBinding.root)
            val btnGroup = dialogBinding.btnGroup
            val options = SharedSavingsGoalManager.TimeSpan.values().map { it.label }.toTypedArray()
            val curRepetition = itemRepetition.text?.removePrefix(repetitionPrefix).toString()

            for (option in options) {

                val btn = RadioButton(this@AddPayment, )
                btn.text = option
                val layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                layoutParams.setMargins(0, 10, 0, 10)
                btn.layoutParams = layoutParams

                if (option == SharedSavingsGoalManager.TimeSpan.None.label && isSavingsGoal) {
                    btn.visibility = View.GONE
                }

                btnGroup.addView(btn)

                if (option == curRepetition) {
                    btnGroup.check(btn.id)
                }
            }

            alert.setPositiveButton(("Yes")) { dialog, _ ->
                var selected = -1
                btnGroup.forEachIndexed { index, view ->
                    if (view is RadioButton && view.isChecked) {
                        selected = index
                        return@forEachIndexed
                    }
                }
                val selectedOption = SharedSavingsGoalManager.TimeSpan.values()[selected]
                itemRepetition.setText(repetitionPrefix + selectedOption.label)
                dialog.dismiss()
            }
            alert.setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }

            alert.show()
        }

        btnSave.setOnClickListener {
            val title = itemTitle.text?.toString()?.trim() ?: ""

            // check if important fields are filled
            if (title.isEmpty() || itemPrice.text.isNullOrEmpty()) {
                Snackbar.make(
                    binding.root,
                    "Please fill the title and price",
                    Snackbar.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {

                val wallet = itemWallet.selectedItem?.toString() ?: ""
                val currency = itemCurrency.selectedItem?.toString() ?: ""
                var price = sharedCurrency.calculateFromCurrency(
                    itemPrice.text!!.toString().toDouble(),
                    currency,
                    applicationContext
                )
                val description = itemDescription.text?.toString() ?: ""
                val dateStart = Date.from(
                    LocalDate.parse(
                        itemDateStart.text.toString(),
                        DateTimeFormatter.ofPattern(dateFormatPattern)
                    ).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()
                )
                var dateEnd = Date.from(
                    LocalDate.parse(
                        itemDateEnd.text.toString(),
                        DateTimeFormatter.ofPattern(dateFormatPattern)
                    ).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()
                )
                val category = itemCategory.text?.toString() ?: ""
                val repetition = SharedSavingsGoalManager.TimeSpan.valueOf(
                    itemRepetition.text?.toString()?.removePrefix(repetitionPrefix) ?: SharedSavingsGoalManager.TimeSpan.None.label
                )
                val startAmount = 0.0
                var endAmount = 0.0

                if (isSavingsGoal) {
                    /* calculate price per payment and dateEnd (else going from 01.06 to 02.07 (Monthly),  will result in 3 payments instead of 2) */
                    endAmount = price
                    val calendar: Calendar = Calendar.getInstance()
                    calendar.time = dateStart
                    val diffInMillis = dateEnd.time - dateStart.time

                    val amountOfTime: Int
                    when (repetition) {
                        SharedSavingsGoalManager.TimeSpan.Daily -> {
                            amountOfTime = (diffInMillis / (1000L * 60 * 60 * 24)).toInt()
                            calendar.add(Calendar.DAY_OF_YEAR, amountOfTime)
                        }
                        SharedSavingsGoalManager.TimeSpan.Weekly -> {
                            amountOfTime = (diffInMillis / (1000L * 60 * 60 * 24 * 7)).toInt()
                            calendar.add(Calendar.WEEK_OF_YEAR, amountOfTime)
                        }
                        SharedSavingsGoalManager.TimeSpan.Monthly -> {
                            amountOfTime = (diffInMillis / (1000L * 60 * 60 * 24 * 30)).toInt()
                            calendar.add(Calendar.MONTH, amountOfTime)
                        }
                        SharedSavingsGoalManager.TimeSpan.Yearly -> {
                            amountOfTime = (diffInMillis / (1000L * 60 * 60 * 24 * 365)).toInt()
                            calendar.add(Calendar.YEAR, amountOfTime)
                        }
                        else -> {
                            amountOfTime = 0
                        }
                    }
                    price /= amountOfTime
                    dateEnd = calendar.time
                }

                var newPayment = Payment(
                    userId = curUser,
                    wallet = wallet,
                    title = title,
                    description = description,
                    price = price,
                    date = dateStart,
                    isPayment = isPayment,
                    category = category,
                    savingsGoalId = curSavingsGoalId,
                )
                if (curItemId != -1) {
                    newPayment = newPayment.copy(id = curItemId)
                }

                var newTitle = sharedSavingsGoal.removeTimSpanTitle(title, repetition, previousRepetition)
                var newRepeatingPayment = SavingsGoal(
                    userId = curUser,
                    wallet = wallet,
                    title = newTitle,
                    description = description,
                    nextDate = dateStart,
                    timeSpanPerTime = repetition,
                    amountPerTime = price,
                    isPayment = isPayment,
                    category = category,
                    endDate = dateEnd,
                    startAmount = startAmount,
                    endAmount = endAmount,
                )
                if (curSavingsGoalId != null) {
                    newRepeatingPayment = newRepeatingPayment.copy(id = curSavingsGoalId!!)
                }

                newTitle = sharedSavingsGoal.timeSpanTitle(newTitle, repetition)

                dbCategory.insertCategory(Category(category))

                if (previousRepetition == SharedSavingsGoalManager.TimeSpan.None && repetition == SharedSavingsGoalManager.TimeSpan.None) {
                    /* is and never was a repeating payment */
                    dbPayment.upsertPayment(newPayment)
                    backButtonCallback.handleOnBackPressed()
                } else if (previousRepetition == SharedSavingsGoalManager.TimeSpan.None && repetition != SharedSavingsGoalManager.TimeSpan.None) {
                    /* it is now a repeating payment */
                    dbCategory.insertCategory(Category(category))
                    dbSavingsGoal.insertSavingsGoal(newRepeatingPayment).toInt()

                    dbPayment.deletePayment(curItemId)
                    sharedSavingsGoal.updateSavingsGoals(applicationContext)
                    backButtonCallback.handleOnBackPressed()
                } else if (previousRepetition != SharedSavingsGoalManager.TimeSpan.None && repetition == SharedSavingsGoalManager.TimeSpan.None) {
                    /* it was a repeating payment */
                    val alert = AlertDialog.Builder(this@AddPayment)
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
                } else if (previousRepetition != SharedSavingsGoalManager.TimeSpan.None && previousRepetition == repetition) {
                    /* this was a repeating payment is staying the same */
                    val dialogBinding = DialogRadioButtonsBinding.inflate(layoutInflater)
                    val alert = AlertDialog.Builder(this@AddPayment)
                    alert.setView(dialogBinding.root)
                    val btnGroup = dialogBinding.btnGroup
                    val options = arrayOf("Change the selected occurrence only", "Change this and all future occurrences", "Change all occurrences")

                    for (option in options) {
                        val btn = RadioButton(this@AddPayment, )
                        btn.text = option
                        val layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        layoutParams.setMargins(0, 10, 0, 10)
                        btn.layoutParams = layoutParams
                        btnGroup.addView(btn)
                    }

                    alert.setPositiveButton(("Yes")) { dialog, _ ->
                        var selected = -1
                        btnGroup.forEachIndexed { index, view ->
                            if (view is RadioButton && view.isChecked) {
                                selected = index
                                return@forEachIndexed
                            }
                        }
                        when (selected) {
                            0 -> {
                                lifecycleScope.launch {
                                    dbPayment.upsertPayment(newPayment.copy(title = newTitle))
                                    backButtonCallback.handleOnBackPressed()
                                }
                            }

                            1 -> {
                                lifecycleScope.launch {
                                    dbPayment.updatePaymentsBySavingsGoalFromPayment(newPayment.wallet, newTitle, newPayment.description!!, newPayment.price, newPayment.isPayment, newPayment.category!!, curSavingsGoalId!!, curItemId)
                                    dbSavingsGoal.updateSavingsGoal(newRepeatingPayment)
                                    backButtonCallback.handleOnBackPressed()
                                }
                            }

                            2 -> {
                                lifecycleScope.launch {
                                    dbPayment.updatePaymentsBySavingsGoal(newPayment.wallet, newTitle, newPayment.description!!, newPayment.price, newPayment.isPayment, newPayment.category!!, curSavingsGoalId!!)
                                    dbSavingsGoal.updateSavingsGoal(newRepeatingPayment)
                                    backButtonCallback.handleOnBackPressed()
                                }
                            }

                            else -> {}
                        }
                        dialog.dismiss()
                    }

                    alert.setNegativeButton("No") { dialog, _ ->
                        dialog.dismiss()
                    }

                    alert.show()
                } else if (previousRepetition != SharedSavingsGoalManager.TimeSpan.None && previousRepetition != repetition) {
                    /* this was a repeating payment but repetition changed */
                    val alert = AlertDialog.Builder(this@AddPayment)
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
                categoryAdapter.notifyDataSetChanged()
            }
        }

        curItemId = intent.getIntExtra("paymentId", -1)

        currencyAdapter = ArrayAdapter(applicationContext, R.layout.spinner_item)
        walletAdapter = ArrayAdapter(applicationContext, R.layout.spinner_item)
        val categorySpinner = binding.categorySpinner

        var startTitle = ""
        var startDateStart = SimpleDateFormat(dateFormatPattern, Locale.getDefault()).format(Date())
        var startDateEnd = SimpleDateFormat(dateFormatPattern, Locale.getDefault()).format(Date(0))
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
                startDateStart = SimpleDateFormat(dateFormatPattern, Locale.getDefault()).format(item.date)
                startDateEnd = SimpleDateFormat(dateFormatPattern, Locale.getDefault()).format(Date(0))
                startPrice = "%.2f".format(item.price)
                startCategory = item.category ?: ""
                startWallet = walletAdapter.getPosition(item.wallet)
                isPayment = item.isPayment
                curSavingsGoalId = item.savingsGoalId
                if (curSavingsGoalId != null) {
                    previousRepetition = dbSavingsGoal.getTimeSpan(curSavingsGoalId!!)
                    val newDateEnd = dbSavingsGoal.getDateEnd(curSavingsGoalId!!)
                    if (Date(0) != newDateEnd) {
                        startDateEnd = SimpleDateFormat(dateFormatPattern, Locale.getDefault()).format(newDateEnd)
                        itemSavingsGoal.performClick()
                    }

                    startRepetition = previousRepetition.label
                }
                startDescription = item.description ?: ""
            }

            // Setup default values
            itemTitle.setText(startTitle)
            itemDateStart.setText(startDateStart)
            itemDateEnd.setText(startDateEnd)
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
