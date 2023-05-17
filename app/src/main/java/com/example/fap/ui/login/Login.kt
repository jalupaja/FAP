package com.example.fap

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.TypedValue
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.example.fap.databinding.ActivityLoginBinding
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */

class Login : AppCompatActivity() {

    private val KEY_ALIAS = "FAP_Biometry_key"
    private val KEYSTORE_PROVIDER = "AndroidKeyStore"

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

    // Implement biometry callbacks
    private val biometricAuthenticationCallback = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            val sharedPreferences = getSharedPreferences(getString(R.string.shared_prefs), MODE_PRIVATE)
            val encryptedCode = sharedPreferences.getString(getString(R.string.shared_prefs_biometrics_key), "")

            if (encryptedCode.isNullOrEmpty()) {
                // Encrypt
                val userCode = textLogin.text.toString()
                if (encryptAndSaveUserCode(userCode) == null) {
                    showBiometricError("Biometric failed")
                }
            } else {
                // Decrypt
                val plaintext = decryptString(encryptedCode)
                if (checkPassword(plaintext)) {
                    login(plaintext)
                } else {
                    showBiometricError("There was a problem logging in. Please redo the biometry")
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

        val sharedPreferences = applicationContext?.getSharedPreferences(getString(R.string.shared_prefs), Context.MODE_PRIVATE)

        registerState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getSerializableExtra ("STATE", REGISTER_STATE::class.java) ?: REGISTER_STATE.REGISTERED
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

        if (! checkRegistered()) {
            registerState = REGISTER_STATE.REGISTERING
            lblLoginStatus.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20F)
            lblLoginStatus.text = getString(R.string.register_password)
        } else if (registerState == REGISTER_STATE.ACTIVATE_BIOMETRICS) {
            lblLoginStatus.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20F)
            lblLoginStatus.text = getString(R.string.activate_biometrics)
        }


        if (sharedPreferences?.getString(getString(R.string.shared_prefs_biometrics_key), "").isNullOrEmpty()) {
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

    /* //TODO if logged in?
    override fun onResume() {
        super.onResume()

        if (ciphertextWrapper != null) {
            /* TODO test if this works with an encrypted DB */
            if (SampleAppUser.fakeToken == null) {
                showBiometricPromptForDecryption()
            } else {
                startActivity(Intent(this, MainActivity::class.java))
            }
        }
    }*/

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
                    // TODO create Database with Password?
                    // TODO send Database to MainActivity
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finishAffinity()
                    lblLoginStatus.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14F)
                    lblLoginStatus.text = ""
                    registerState = REGISTER_STATE.REGISTERED
                } else {
                    lblLoginStatus.text = getString(R.string.retry_register_password)
                    registerState = REGISTER_STATE.REGISTERING
                }
            }
            REGISTER_STATE.ACTIVATE_BIOMETRICS -> {
                if (textLogin.text!!.toString() == "000") {  // TODO check if passwords matches database?
                    authenticateWithBiometrics()

                    // TODO FIXME the editor doesnt work
                    //startActivity(Intent(this, MainActivity::class.java))
                    //finishAffinity()
                    // TODO add status text on biometrics
                    lblLoginStatus.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14F)
                    lblLoginStatus.text = ""
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

    private fun login(key: String) {
        // TODO send Database to MainActivity
        startActivity(Intent(this, MainActivity::class.java))
        finishAffinity()
        lblLoginStatus.text = ""
    }

    private fun authenticateWithBiometrics() {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.app_name))
            .setSubtitle(getString(R.string.biometric_subtitle))
            .setConfirmationRequired(false)
            .setNegativeButtonText(getString(R.string.biometric_dont_use))
            .build()

        val biometricPrompt = BiometricPrompt(this, mainExecutor, biometricAuthenticationCallback)

        biometricPrompt.authenticate(promptInfo)
    }

    private fun decryptString(encryptedString: String): String {
        val cipher = getCipher()
        val encryptedData = Base64.decode(encryptedString, Base64.DEFAULT)
        val decryptedBytes = cipher?.doFinal(encryptedData)
        return if (decryptedBytes != null) {
            String(decryptedBytes, StandardCharsets.UTF_8)
        } else {
            ""
        }
    }

    private fun encryptAndSaveUserCode(userCode: String): ByteArray? {
        val cipher = getCipher()
        cipher?.let {
            val encryptedData = cipher.doFinal(userCode.toByteArray())
            saveEncryptedCode(encryptedData)
            return encryptedData
        }
        return null
    }

    private fun getCipher(): Cipher? {
        val key = getKey()
        return try {
            val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
            cipher.init(Cipher.ENCRYPT_MODE, key)
            cipher
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getKey(): SecretKey {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER)
        keyStore.load(null)
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER)
            val keyGenParameterSpec = KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .setUserAuthenticationRequired(true)
                .build()
            keyGenerator.init(keyGenParameterSpec)
            keyGenerator.generateKey()
        }
        return keyStore.getKey(KEY_ALIAS, null) as SecretKey
    }

    private fun saveEncryptedCode(encryptedCode: ByteArray) {
        val encryptedData = Base64.encodeToString(encryptedCode, Base64.DEFAULT)

        val sharedPreferences = getSharedPreferences(getString(R.string.shared_prefs), MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(getString(R.string.shared_prefs_biometrics_key), encryptedData)
        editor.apply()

        startActivity(Intent(this, MainActivity::class.java))
        finishAffinity()
    }

    private fun showBiometricError(message: String) {
        val snackbar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
        snackbar.setActionTextColor(ContextCompat.getColor(this, com.google.android.material.R.color.design_default_color_error))
        snackbar.show()
    }
}