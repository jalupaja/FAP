package com.example.fap.ui.dialogs

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.fap.R
import com.example.fap.data.FapDatabase
import com.example.fap.data.Wallet
import com.example.fap.databinding.DialogAddWalletBinding
import com.example.fap.utils.SharedPreferencesManager
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class AddWallet : AppCompatActivity() {

    private lateinit var binding: DialogAddWalletBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // create add wallet dialog
        binding = DialogAddWalletBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val input = binding.addWalletInput
        val btnSave  = binding.btnSave
        val btnBack = binding.btnBack

        btnSave.setOnClickListener {
            if (input.text.toString().isEmpty()) {
                Snackbar.make(
                    binding.root,
                    "Please enter a new Wallet",
                    Snackbar.LENGTH_SHORT
                ).show()
            } else {
                lifecycleScope.launch {
                    val db = FapDatabase.getInstance(applicationContext).fapDao()
                    val curUser = SharedPreferencesManager.getInstance(applicationContext)
                        .getCurUser(applicationContext)
                    db.insertWallet(Wallet(input.text.toString(), curUser))
                }
                backButtonCallback.handleOnBackPressed()
            }
        }

        btnBack.setOnClickListener {
            backButtonCallback.handleOnBackPressed()
        }
    }

    private val backButtonCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            finish()
        }
    }
}