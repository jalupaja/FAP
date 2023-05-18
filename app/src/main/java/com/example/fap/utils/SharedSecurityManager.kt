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
import androidx.biometric.BiometricPrompt
import com.example.fap.R
import java.util.concurrent.Executor

class SharedSecurityManager(context: Context) {

    private val KEYSTORE_PROVIDER = "AndroidKeyStore"
    private val sharedPreferences:SharedPreferencesManager = SharedPreferencesManager.getInstance(context)
    private var context: Context = context

    /* SYNCHRONOUS ENCRYPTION
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