package com.example.fap

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
import com.example.fap.utils.SharedPreferencesManager
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import java.security.KeyStore
import javax.crypto.Cipher
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */

class Login : AppCompatActivity() {

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

    private lateinit var sharedPreferences: SharedPreferencesManager

    // Implement biometry callbacks
    private val biometricAuthenticationCallback =
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                val encryptedCode =
                    sharedPreferences.getString(getString(R.string.shared_prefs_biometrics_key), "")

                if (encryptedCode.isNullOrEmpty()) {
                    // Encrypt
                    if (!startEncryption(tmpPass)) {
                        showBiometricError("Biometric failed")
                    } else {
                        backButtonCallback.handleOnBackPressed()
                    }
                } else {
                    // Decrypt
                    val plaintext = startDecryption(encryptedCode)
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

        sharedPreferences = SharedPreferencesManager.getInstance(applicationContext)

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


        if (sharedPreferences.getString(getString(R.string.shared_prefs_biometrics_key), "")
                .isNullOrEmpty()
        ) {
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

    /* Sync Encryption tries
    private fun startEncryption(str: String): Boolean {
        val key = getKey(getString(R.string.shared_prefs_biometrics_key))
        val encryptedString = encryptString(key, str)
        if (encryptedString.isNotEmpty()) {
            saveEncryptedString(encryptedString)
            return true
        }
        return false
    }

    private fun startDecryption(str: String): String {
        val key = getKey(getString(R.string.shared_prefs_biometrics_key))
        return decryptString(key, str)
    }

    fun encryptString2(key: SecretKey, data: String): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val iv: ByteArray = generateRandomIV(16)
        cipher.init(Cipher.ENCRYPT_MODE, key, IvParameterSpec(iv))
        sharedPreferences.saveString(getString(R.string.shared_prefs_biometrics_iv), iv.toString())
        val encodedBytes = cipher.doFinal(data.toByteArray())
        return Base64.encodeToString(encodedBytes, Base64.NO_WRAP)
    }

    fun encryptString(key: SecretKey, data: String): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val iv: ByteArray = generateRandomIV(16)
        cipher.init(Cipher.ENCRYPT_MODE, key, IvParameterSpec(iv))
        val encodedBytes = cipher.doFinal(data.toByteArray())
        val encryptedData = ByteArray(iv.size + encodedBytes.size)
        System.arraycopy(iv, 0, encryptedData, 0, iv.size)
        System.arraycopy(encodedBytes, 0, encryptedData, iv.size, encodedBytes.size)
        return Base64.encodeToString(encryptedData, Base64.NO_WRAP)
    }

    fun decryptString(key: SecretKey, encryptedData: String): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val encryptedBytes = Base64.decode(encryptedData, Base64.NO_WRAP)

        val ivSize = 16 // Assuming a 16-byte IV was used
        val iv = ByteArray(ivSize)
        System.arraycopy(encryptedBytes, 0, iv, 0, ivSize)

        val encodedBytes = ByteArray(encryptedBytes.size - ivSize)
        System.arraycopy(encryptedBytes, ivSize, encodedBytes, 0, encodedBytes.size)

        cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(iv))
        val decodedBytes = cipher.doFinal(encodedBytes)
        return String(decodedBytes)
    }

    fun decryptString2(key: SecretKey, encrypted: String): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val spec = GCMParameterSpec(128, sharedPreferences.getString(getString(R.string.shared_prefs_biometrics_iv), "").toByteArray())
        cipher.init(Cipher.DECRYPT_MODE, key, spec)
        val encodedBytes = Base64.decode(encrypted, Base64.NO_WRAP)
        val decoded = cipher.doFinal(encodedBytes)
        return String(decoded, Charsets.UTF_8)
    }

    fun generateRandomIV(ivSize: Int): ByteArray {
        val secureRandom = SecureRandom()
        val iv = ByteArray(ivSize)
        secureRandom.nextBytes(iv)
        return iv
    }

    private fun getKey(keyAlias: String): SecretKey {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER)
        keyStore.load(null)

        val keyEntry = keyStore.getEntry(keyAlias, null) as? KeyStore.SecretKeyEntry

        return if (keyEntry != null) {
            keyEntry.secretKey
        } else {
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER)
            val spec = KeyGenParameterSpec.Builder(
                keyAlias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setKeySize(256)
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .build()

            keyGenerator.init(spec)
            keyGenerator.generateKey()
        }
    } */

    /* ASYNC ENCRYPTION */
    private fun startEncryption(str: String): Boolean {
        val (pub_key, _) = getKeys(getString(R.string.shared_prefs_biometrics_key))
        if (pub_key != null) {
            val encryptedString = encryptString(pub_key, str)
            if (encryptedString.isNullOrEmpty()) {
                return false
            } else {
                saveEncryptedString(encryptedString)
                return true
            }
        }
        return false
    }

    private fun startDecryption(str: String): String {
        val (_, priv_key) = getKeys(getString(R.string.shared_prefs_biometrics_key))
        if (priv_key != null) {
            return decryptString(priv_key, str)
        }
        return ""
    }

    private fun encryptString(publicKey: PublicKey, str: String): String {
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        val encryptedBytes = cipher.doFinal(str.toByteArray())
        return Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
    }

    private fun decryptString(privateKey: PrivateKey, encryptedString: String): String {
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.DECRYPT_MODE, privateKey)
        val encryptedBytes = Base64.decode(encryptedString, Base64.DEFAULT)
        val decryptedBytes = cipher.doFinal(encryptedBytes)
        return String(decryptedBytes)
    }

    private fun getKeys(keyAlias: String): Pair<PublicKey?, PrivateKey?> {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER)
        keyStore.load(null)

        val privateKey = keyStore.getKey(keyAlias, null) as? PrivateKey
        val publicKey = keyStore.getCertificate(keyAlias)?.publicKey

        if (privateKey == null || publicKey == null) {
            val keyPairGenerator =
                KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, KEYSTORE_PROVIDER)
            val spec = KeyGenParameterSpec.Builder(
                keyAlias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setKeySize(2048)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                .build()

            keyPairGenerator.initialize(spec)
            val keyPair = keyPairGenerator.generateKeyPair()
            return Pair(keyPair.public, keyPair.private)
        }

        return Pair(publicKey, privateKey)
    }

    private fun saveEncryptedString(str: String) {
        sharedPreferences.saveString(getString(R.string.shared_prefs_biometrics_key), str)
    }

    private fun showBiometricError(message: String) {
        val snackbar =
            Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
        snackbar.setActionTextColor(
            ContextCompat.getColor(
                this,
                com.google.android.material.R.color.design_default_color_error
            )
        )
        snackbar.show()
    }
}