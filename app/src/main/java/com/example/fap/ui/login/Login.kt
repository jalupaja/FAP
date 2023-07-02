package com.example.fap.ui.login

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.biometric.BiometricPrompt
import androidx.lifecycle.lifecycleScope
import com.example.fap.MainActivity
import com.example.fap.R
import com.example.fap.data.FapDatabase
import com.example.fap.data.entities.Category
import com.example.fap.data.entities.User
import com.example.fap.data.entities.Wallet
import com.example.fap.databinding.ActivityLoginBinding
import com.example.fap.utils.SharedCurrencyManager
import com.example.fap.utils.SharedPreferencesManager
import com.example.fap.utils.SharedSavingsGoalManager
import com.example.fap.utils.SharedSecurityManager
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.UUID

class Login : AppCompatActivity() {

    companion object {
        enum class REGISTERSTATE {
            REGISTERED,
            REGISTERING,
            CONFIRMING,
            ACTIVATE_BIOMETRICS,
        }
    }

    private lateinit var registerState: REGISTERSTATE
    private var tmpPass: String = ""
    private lateinit var binding: ActivityLoginBinding
    private lateinit var lblLoginStatus: TextView
    private lateinit var textLogin: TextInputEditText
    private lateinit var btn1: Button
    private lateinit var btn2: Button
    private lateinit var btn3: Button
    private lateinit var btn4: Button
    private lateinit var btn5: Button
    private lateinit var btn6: Button
    private lateinit var btn7: Button
    private lateinit var btn8: Button
    private lateinit var btn9: Button
    private lateinit var btnRemove: ImageButton
    private lateinit var btn0: Button
    private lateinit var btnLogin: ImageButton

    private lateinit var sharedPreferences: SharedPreferencesManager
    private lateinit var sharedSecurity: SharedSecurityManager

    // Implement biometry callbacks
    private val biometricAuthenticationCallback = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            val encryptedCode = sharedPreferences.getString(getString(R.string.shared_prefs_biometrics_key))

            if (encryptedCode.isEmpty()) {
                // Encrypt
                if (!sharedSecurity.startEncryption(tmpPass, applicationContext)) {
                    sharedSecurity.showBiometricError(findViewById(android.R.id.content), "Biometric failed", applicationContext)
                } else {
                    backButtonCallback.handleOnBackPressed()
                }
            } else {
                // Decrypt
                val plaintext = sharedSecurity.startDecryption(encryptedCode, applicationContext)
                if (checkPassword(plaintext)) {
                    login()
                } else {
                    sharedSecurity.showBiometricError(findViewById(android.R.id.content), "There was a problem logging in. Please redo the biometry", applicationContext)
                }
            }
        }
    }

    // Implement Back Button
    private val backButtonCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            when (registerState) {
                REGISTERSTATE.CONFIRMING -> {
                    textLogin.text!!.clear()
                    registerState = REGISTERSTATE.REGISTERING
                    lblLoginStatus.text = getString(R.string.register_password)
                }

                else -> {
                    finish()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = SharedPreferencesManager.getInstance(applicationContext)
        sharedSecurity = SharedSecurityManager.getInstance(applicationContext)

        val currentTheme = sharedPreferences.getInt(getString(R.string.shared_prefs_theme), -1)
        if (AppCompatDelegate.getDefaultNightMode() != currentTheme) {
            AppCompatDelegate.setDefaultNightMode(currentTheme)
            recreate()
        }

        registerState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getSerializableExtra("STATE", REGISTERSTATE::class.java)
                ?: REGISTERSTATE.REGISTERED
        } else {
            @Suppress("DEPRECATION")
            intent?.getSerializableExtra("STATE") as? REGISTERSTATE ?: REGISTERSTATE.REGISTERED
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        lblLoginStatus = binding.lblLoginStatus
        textLogin = binding.passwordInput
        btnLogin = binding.btnLoginAccept
        btn1 = binding.btnLogin1
        btn2 = binding.btnLogin2
        btn3 = binding.btnLogin3
        btn4 = binding.btnLogin4
        btn5 = binding.btnLogin5
        btn6 = binding.btnLogin6
        btn7 = binding.btnLogin7
        btn8 = binding.btnLogin8
        btn9 = binding.btnLogin9
        btn0 = binding.btnLogin0
        btnRemove = binding.btnLoginBack

        btn1.setOnClickListener {
            textLogin.append("1")
        }
        btn2.setOnClickListener {
            textLogin.append("2")
        }
        btn3.setOnClickListener {
            textLogin.append("3")
        }
        btn4.setOnClickListener {
            textLogin.append("4")
        }
        btn5.setOnClickListener {
            textLogin.append("5")
        }
        btn6.setOnClickListener {
            textLogin.append("6")
        }
        btn7.setOnClickListener {
            textLogin.append("7")
        }
        btn8.setOnClickListener {
            textLogin.append("8")
        }
        btn9.setOnClickListener {
            textLogin.append("9")
        }
        btn0.setOnClickListener {
            textLogin.append("0")
        }

        btnRemove.setOnClickListener {
            val current = textLogin.text?.toString()
            if (!current.isNullOrEmpty()) {
                textLogin.setText(current.substring(0, current.length - 1))
            }
        }

        textLogin.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_UNSPECIFIED) { /* Arrow on mobile || Enter on Desktop */
                tryLogin()
                true
            } else {
                false
            }
        }

        btnLogin.setOnClickListener {
            tryLogin()
        }

        textLogin.setText("")

        if (!checkRegistered(applicationContext)) {
            registerState = REGISTERSTATE.REGISTERING
            lblLoginStatus.text = getString(R.string.register_password)
        } else if (registerState == REGISTERSTATE.ACTIVATE_BIOMETRICS) {
            lblLoginStatus.text = getString(R.string.activate_biometrics)
        }


        if (sharedPreferences.getString(getString(R.string.shared_prefs_biometrics_key)).isEmpty() ) {
            binding.useBiometrics.visibility = View.INVISIBLE
        } else {
            authenticateWithBiometrics()
            binding.useBiometrics.visibility = View.VISIBLE
        }

        binding.useBiometrics.setOnClickListener {
            authenticateWithBiometrics()
        }

        onBackPressedDispatcher.addCallback(this, backButtonCallback)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Fix Back Button in Toolbar
        if (item.itemId == android.R.id.home) {
            backButtonCallback.handleOnBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun tryLogin() {

        // Disallow 0 length Passwords
        if (textLogin.text.isNullOrEmpty()) {
            return
        }

        when (registerState) {
            REGISTERSTATE.REGISTERED -> {
                if (checkPassword(textLogin.text.toString())) {
                    lifecycleScope.launch {
                        SharedSavingsGoalManager.getInstance(applicationContext).updateSavingsGoals(applicationContext)
                        login()
                        SharedCurrencyManager.getInstance(applicationContext).tryUpdateCurrency(applicationContext)
                    }
                } else {
                    lblLoginStatus.text = getString(R.string.wrong_password)
                }
            }

            REGISTERSTATE.REGISTERING -> {
                tmpPass = textLogin.text!!.toString()
                lblLoginStatus.text = getString(R.string.confirm_password)
                registerState = REGISTERSTATE.CONFIRMING
            }

            REGISTERSTATE.CONFIRMING -> {
                if (textLogin.text!!.toString() == tmpPass) {
                    registerState = REGISTERSTATE.REGISTERED

                    val userId = UUID.randomUUID().toString()
                    sharedPreferences.saveCurUser(applicationContext, userId)
                    sharedPreferences.saveString(application.getString(R.string.shared_prefs_current_currency), "€")

                    // save password hash
                    sharedPreferences.saveString(getString(R.string.shared_prefs_hash), calculateHash(tmpPass))
                    MainScope().launch {
                        // create Database using the Password
                        val db = FapDatabase.getInstance(applicationContext, tmpPass)
                        // setup default values
                        db.fapDaoUser().insertUser(User(userId))
                        db.fapDaoCategory().insertCategory(Category(name = "")) // use as 'not categorised' to avoid FOREIGN KEY constraint fails
                        db.fapDaoCategory().insertCategory(Category(name = "Groceries"))
                        db.fapDaoCategory().insertCategory(Category(name = "Income"))
                        db.fapDaoWallet().insertWallet(Wallet(userId = userId, name = "Bank"))
                        db.fapDaoWallet().insertWallet(Wallet(userId = userId, name = "Cash"))

                        SharedCurrencyManager.getInstance(applicationContext).initCurrency(applicationContext)
                    }
                    login()
                } else {
                    lblLoginStatus.text = getString(R.string.retry_register_password)
                    registerState = REGISTERSTATE.REGISTERING
                }
            }

            REGISTERSTATE.ACTIVATE_BIOMETRICS -> {
                if (checkPassword(textLogin.text!!.toString())) {
                    tmpPass = textLogin.text!!.toString()
                    lblLoginStatus.text = ""
                    authenticateWithBiometrics()
                } else {
                    lblLoginStatus.text = getString(R.string.wrong_password)
                }
            }
        }
        textLogin.text!!.clear()
    }

    private fun checkRegistered(context: Context): Boolean {
        val ctx = context.applicationContext
        return ctx.getDatabasePath(ctx.getString(R.string.database_name)).exists()
    }

    private fun checkPassword(password: String): Boolean {
        return if (calculateHash(password) == sharedPreferences.getString(getString(R.string.shared_prefs_hash))) {
            FapDatabase.getInstance(applicationContext, password)
            true
        } else {
            false
        }
    }

    private fun calculateHash(str: String): String {
        val messageDigest = MessageDigest.getInstance("SHA-512")
        val byteHash = messageDigest.digest(str.toByteArray())
        return byteHash.joinToString("") { "%02x".format(it) }
    }

    private fun authenticateWithBiometrics() {
        sharedSecurity.authenticateWithBiometrics(this, mainExecutor, biometricAuthenticationCallback, applicationContext)
    }

    private fun login() {
        startActivity(Intent(this, MainActivity::class.java))
        finishAffinity()
        lblLoginStatus.text = ""
    }
}
