package com.example.fap.utils

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import java.security.KeyStore
import javax.crypto.Cipher
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import android.content.Context
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import com.example.fap.R
import java.util.concurrent.Executor

class SharedSecurityManager(context: Context) {

    private val KEYSTORE_PROVIDER = "AndroidKeyStore"
    private val sharedPreferences:SharedPreferencesManager = SharedPreferencesManager.getInstance(context)
    private var context: Context = context

    /* ASYNCHRONOUS ENCRYPTION */
    public fun startEncryption(str: String): Boolean {
        val (pub_key, _) = getKeys(context.getString(R.string.shared_prefs_biometrics_key))
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

    public fun startDecryption(str: String): String {
        val (_, priv_key) = getKeys(context.getString(R.string.shared_prefs_biometrics_key))
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
        sharedPreferences.saveString(context.getString(R.string.shared_prefs_biometrics_key), str)
    }

    public fun checkBiometric(): Boolean {
        return BiometricManager.from(context).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS
    }

    public fun authenticateWithBiometrics(
        parent: AppCompatActivity,
        callback: Executor,
        biometricAuthenticationCallback: BiometricPrompt.AuthenticationCallback
    ) {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(context.getString(R.string.app_name))
            .setSubtitle(context.getString(R.string.biometric_subtitle))
            .setConfirmationRequired(true)
            .setNegativeButtonText(context.getString(R.string.biometric_dont_use))
            .build()

        val biometricPrompt = BiometricPrompt(parent, callback, biometricAuthenticationCallback)

        biometricPrompt.authenticate(promptInfo)
    }

    public fun showBiometricError(view: View, message: String) {
        val snackbar =
            Snackbar.make(view, message, Snackbar.LENGTH_LONG)
        snackbar.setActionTextColor(
            ContextCompat.getColor(
                context,
                com.google.android.material.R.color.design_default_color_error
            )
        )
        snackbar.show()
    }

    companion object {
        private var instance: SharedSecurityManager? = null

        fun getInstance(context: Context): SharedSecurityManager {
            if (instance == null) {
                instance = SharedSecurityManager(context)
            }
            return instance as SharedSecurityManager
        }
    }
}
