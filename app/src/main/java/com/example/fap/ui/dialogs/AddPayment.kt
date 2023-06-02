package com.example.fap.ui.dialogs

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.fap.R
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
import com.google.android.material.snackbar.Snackbar
import java.time.ZoneId
import java.util.*

class AddPayment : AppCompatActivity() {

    private lateinit var binding: ActivityAddPaymentBinding
    private lateinit var walletAdapter: ArrayAdapter<String>
    private lateinit var categoryAdapter: ArrayAdapter<String>
    private var isPayment: Boolean = true
    private lateinit var sharedPreferences: SharedPreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        sharedPreferences = SharedPreferencesManager.getInstance(applicationContext)
        val db = FapDatabase.getInstance(applicationContext).fapDao()
        val dateFormatPattern = "dd.MM.yyyy"
        val curUser = sharedPreferences.getCurUser(applicationContext)

        // Set default values
        val datePicker = binding.datePicker
        val currentDate = SimpleDateFormat(dateFormatPattern, Locale.getDefault()).format(Date())
        datePicker.setText(currentDate)
        datePicker.setOnClickListener {

            val curDate = LocalDate.parse(datePicker.text.toString(), DateTimeFormatter.ofPattern(dateFormatPattern))
            val datePickerDialog = DatePickerDialog(this@AddPayment, { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(Calendar.YEAR, year)
                selectedDate.set(Calendar.MONTH, month)
                selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val formattedDate = SimpleDateFormat(dateFormatPattern, Locale.getDefault()).format(selectedDate.time)

                datePicker.setText(formattedDate)
            }, curDate.year, curDate.monthValue, curDate.dayOfMonth)

            datePickerDialog.show()
        }

        val walletSpinner = binding.walletSpinner
        lifecycleScope.launch {
            val wallets = db.getWallets(curUser)
            walletAdapter = ArrayAdapter(applicationContext, android.R.layout.simple_spinner_dropdown_item)
            walletSpinner.adapter = walletAdapter
            for (wallet in wallets) {
                walletAdapter.add(wallet.name)
            }
        }

        val categorySpinner = binding.categorySpinner
        lifecycleScope.launch {
            val categories = db.getCategories()
            categoryAdapter = ArrayAdapter<String>(applicationContext, android.R.layout.simple_dropdown_item_1line)
            categorySpinner.setAdapter(categoryAdapter)
            for (category in categories) {
                categoryAdapter.add(category.name)
            }
        }

        val btnIsPayment = binding.btnIsPayment
        val btnIsIncome = binding.btnIsIncome

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

        val btnSave = binding.btnSave
        btnSave.setOnClickListener {
            val wallet = walletSpinner.selectedItem?.toString() ?: ""
            val title = binding.titleInput.text?.toString() ?: ""
            val price = binding.priceInput.text?.toString() ?: ""
            val description = binding.descriptionInput.text?.toString() ?: ""
            val date = Date.from(LocalDate.parse(datePicker.text.toString(), DateTimeFormatter.ofPattern(dateFormatPattern)).atStartOfDay().atZone( ZoneId.systemDefault()).toInstant())
            val category = categorySpinner.text?.toString() ?: ""

            // check if important fields are filled
            if (title.isEmpty() || price.isEmpty()) {
                Snackbar.make(binding.root, "Please fill the title and price", Snackbar.LENGTH_SHORT).show()
            } else {
                lifecycleScope.launch {
                    db.insertCategory(Category(category))
                    db.insertPayment(
                        Payment(
                            userId = curUser,
                            wallet = wallet,
                            title = title,
                            description = description,
                            price = price.toDouble(),
                            date = date,
                            isPayment = isPayment,
                            category = category,
                        )
                    )
                }
                backButtonCallback.handleOnBackPressed()
            }
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
