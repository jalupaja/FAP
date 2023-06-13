package com.example.fap.ui.dialogs

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.fap.data.Category
import com.example.fap.data.FapDatabase
import com.example.fap.databinding.ActivityAddPaymentBinding
import com.example.fap.utils.SharedPreferencesManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import android.app.DatePickerDialog
import android.view.MenuItem
import android.widget.ArrayAdapter
import androidx.activity.OnBackPressedCallback
import com.example.fap.data.Payment
import com.example.fap.utils.SharedCurrencyManager
import com.google.android.material.snackbar.Snackbar
import java.time.ZoneId
import java.util.*

class AddPayment : AppCompatActivity() {

    private lateinit var binding: ActivityAddPaymentBinding
    private lateinit var currencyAdapter: ArrayAdapter<String>
    private lateinit var walletAdapter: ArrayAdapter<String>
    private lateinit var categoryAdapter: ArrayAdapter<String>
    private var isPayment: Boolean = true
    private lateinit var sharedPreferences: SharedPreferencesManager
    private lateinit var sharedCurrency: SharedCurrencyManager
    private var curItemId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        sharedPreferences = SharedPreferencesManager.getInstance(applicationContext)
        sharedCurrency = SharedCurrencyManager.getInstance(applicationContext)

        val db = FapDatabase.getInstance(applicationContext).fapDao()
        val curUser = sharedPreferences.getCurUser(applicationContext)
        val dateFormatPattern = "dd.MM.yyyy"

        val itemTitle = binding.titleInput
        val itemDate = binding.datePicker
        val itemPrice = binding.priceInput
        val itemCurrency = binding.currencySpinner
        val itemCategory = binding.categorySpinner
        val itemWallet = binding.walletSpinner
        val btnIsPayment = binding.btnIsPayment
        val btnIsIncome = binding.btnIsIncome
        val itemDescription = binding.descriptionInput
        val btnSave = binding.btnSave

        // onClickListeners
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
                selectedDate.set(Calendar.MONTH, month)
                selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val formattedDate = SimpleDateFormat(dateFormatPattern, Locale.getDefault()).format(selectedDate.time)

                itemDate.setText(formattedDate)
            }, curDate.year, curDate.monthValue, curDate.dayOfMonth)

            datePickerDialog.show()
        }

        btnSave.setOnClickListener {
            val wallet = itemWallet.selectedItem?.toString() ?: ""
            val title = itemTitle.text?.toString() ?: ""
            val price = itemPrice.text?.toString() ?: ""
            val currency = itemCurrency.selectedItem?.toString() ?: ""
            val description = itemDescription.text?.toString() ?: ""
            val date = Date.from(LocalDate.parse(itemDate.text.toString(), DateTimeFormatter.ofPattern(dateFormatPattern)).atStartOfDay().atZone( ZoneId.systemDefault()).toInstant())
            val category = itemCategory.text?.toString() ?: ""

            // check if important fields are filled
            if (title.isEmpty() || price.isEmpty()) {
                Snackbar.make(binding.root, "Please fill the title and price", Snackbar.LENGTH_SHORT).show()
            } else {
                lifecycleScope.launch {
                    var newPayment = Payment(
                        userId = curUser,
                        wallet = wallet,
                        title = title,
                        description = description,
                        price = sharedCurrency.calculateFromCurrency(price.toDouble(), currency, applicationContext),
                        date = date,
                        isPayment = isPayment,
                        category = category,
                    )
                    if (curItemId != -1) {
                        newPayment = newPayment.copy(id = curItemId)
                    }
                    db.insertCategory(Category(category))
                    db.upsertPayment(newPayment)
                }
                backButtonCallback.handleOnBackPressed()
            }
        }

        curItemId = intent.getIntExtra("paymentId", -1)

        currencyAdapter = ArrayAdapter(applicationContext, android.R.layout.simple_spinner_dropdown_item)
        walletAdapter = ArrayAdapter(applicationContext, android.R.layout.simple_spinner_dropdown_item)
        val categorySpinner = binding.categorySpinner

        var startTitle = ""
        var startDate = SimpleDateFormat(dateFormatPattern, Locale.getDefault()).format(Date())
        var startPrice = ""
        val startCurrency = sharedCurrency.getDefaultCurrencyIndex()
        var startCategory = ""
        var startWallet = 0
        var startDescription = ""

        lifecycleScope.launch {
            // Setup existing wallets
            currencyAdapter.addAll(sharedCurrency.getAvailableCurrencies())
            itemCurrency.adapter = currencyAdapter
            val wallets = db.getWallets(curUser)
            itemWallet.adapter = walletAdapter
            for (wallet in wallets) {
                walletAdapter.add(wallet.name)
            }

            // Setup existing categories
            val categories = db.getCategories()
            categoryAdapter = ArrayAdapter<String>(applicationContext, android.R.layout.simple_dropdown_item_1line)
            categorySpinner.setAdapter(categoryAdapter)
            for (category in categories) {
                categoryAdapter.add(category.name)
            }

            // Update default values if this is supposed to edit an existing item
            if (curItemId != -1) {
                val item = db.getPayment(curItemId)
                startTitle = item.title
                startDate = SimpleDateFormat(dateFormatPattern, Locale.getDefault()).format(item.date)
                startPrice = "%.2f".format(item.price.toString())
                startCategory = item.category ?: ""
                startWallet = walletAdapter.getPosition(item.wallet)
                isPayment = item.isPayment
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
