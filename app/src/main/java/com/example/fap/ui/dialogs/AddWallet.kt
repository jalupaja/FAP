package com.example.fap.ui.dialogs

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.fap.data.FapDatabase
import com.example.fap.data.entities.Wallet
import com.example.fap.databinding.DialogAddWalletBinding
import com.example.fap.utils.SharedPreferencesManager
import com.google.android.material.snackbar.Snackbar
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
                    val dbWallet = FapDatabase.getInstance(applicationContext).fapDaoWallet()
                    val curUser = SharedPreferencesManager.getInstance(applicationContext)
                        .getCurUser(applicationContext)
                    dbWallet.insertWallet(Wallet(input.text.toString(), curUser))
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