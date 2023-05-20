package com.example.fap

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
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
import com.example.fap.databinding.ActivityLoginBinding
import com.example.fap.utils.SharedPreferencesManager
import com.example.fap.utils.SharedSecurityManager
import com.google.android.material.textfield.TextInputEditText

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */

class Login : AppCompatActivity() {

    companion object {
        enum class REGISTER_STATE {
            REGISTERED,
            REGISTERING,
            CONFIRMING,
            ACTIVATE_BIOMETRICS,
        }
    }

    private lateinit var registerState: REGISTER_STATE
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

                if (encryptedCode.isNullOrEmpty()) {
                    // Encrypt
                    if (!sharedSecurity.startEncryption(tmpPass)) {
                        sharedSecurity.showBiometricError(findViewById(android.R.id.content), "Biometric failed")
                    } else {
                        backButtonCallback.handleOnBackPressed()
                    }
                } else {
                    // Decrypt
                    val plaintext = sharedSecurity.startDecryption(encryptedCode)
                    if (checkPassword(plaintext)) {
                        login(plaintext)
                    } else {
                        sharedSecurity.showBiometricError(findViewById(android.R.id.content), "There was a problem logging in. Please redo the biometry")
                    }
                }
            }
        }

    // Implement Back Button
    private val backButtonCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            when (registerState) {
                REGISTER_STATE.CONFIRMING -> {
                    textLogin.text!!.clear()
                    registerState = REGISTER_STATE.REGISTERING
                    lblLoginStatus.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20F)
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

        val currentTheme = sharedPreferences.getInt(getString(R.string.shared_prefs_theme))
        if (AppCompatDelegate.getDefaultNightMode() != currentTheme) {
            AppCompatDelegate.setDefaultNightMode(currentTheme)
            recreate()
        }

        registerState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getSerializableExtra("STATE", REGISTER_STATE::class.java)
                ?: REGISTER_STATE.REGISTERED
        } else {
            @Suppress("DEPRECATION")
            intent?.getSerializableExtra("STATE") as? REGISTER_STATE ?: REGISTER_STATE.REGISTERED
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

        if (!checkRegistered()) {
            registerState = REGISTER_STATE.REGISTERING
            lblLoginStatus.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20F)
            lblLoginStatus.text = getString(R.string.register_password)
        } else if (registerState == REGISTER_STATE.ACTIVATE_BIOMETRICS) {
            lblLoginStatus.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20F)
            lblLoginStatus.text = getString(R.string.activate_biometrics)
        }


        if (sharedPreferences.getString(getString(R.string.shared_prefs_biometrics_key)) .isNullOrEmpty() ) {
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
            REGISTER_STATE.REGISTERED -> {
                if (checkPassword(textLogin.text.toString())) {
                    login(textLogin.text.toString())
                } else {
                    lblLoginStatus.text = getString(R.string.wrong_password)
                }
            }

            REGISTER_STATE.REGISTERING -> {
                tmpPass = textLogin.text!!.toString()
                lblLoginStatus.text = getString(R.string.confirm_password)
                registerState = REGISTER_STATE.CONFIRMING
            }

            REGISTER_STATE.CONFIRMING -> {
                if (textLogin.text!!.toString() == tmpPass) {
                    lblLoginStatus.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14F)
                    registerState = REGISTER_STATE.REGISTERED
                    // TODO create Database with Password?
                    login(tmpPass)
                } else {
                    lblLoginStatus.text = getString(R.string.retry_register_password)
                    registerState = REGISTER_STATE.REGISTERING
                }
            }

            REGISTER_STATE.ACTIVATE_BIOMETRICS -> {
                if (checkPassword(textLogin.text!!.toString())) {
                    tmpPass = textLogin.text!!.toString()
                    lblLoginStatus.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14F)
                    lblLoginStatus.text = ""
                    authenticateWithBiometrics()
                } else {
                    lblLoginStatus.text = getString(R.string.wrong_password)
                }
            }
        }
        textLogin.text!!.clear()
    }

    private fun checkRegistered(): Boolean {
        return true // TODO check if database file exists OR use shared prefs
    }

    private fun checkPassword(password: String): Boolean {
        return password == "000" // TODO check if passwords matches database?
    }

    private fun authenticateWithBiometrics() {
        sharedSecurity.authenticateWithBiometrics(this, mainExecutor, biometricAuthenticationCallback)
    }

    private fun login(key: String) {
        // TODO send Database to MainActivity
        startActivity(Intent(this, MainActivity::class.java))
        finishAffinity()
        lblLoginStatus.text = ""
    }
}