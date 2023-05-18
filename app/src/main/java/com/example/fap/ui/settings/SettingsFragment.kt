package com.example.fap.ui.settings

import android.content.Intent
import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.example.fap.Login
import com.example.fap.R
import com.example.fap.utils.SharedPreferencesManager

class SettingsFragment : PreferenceFragmentCompat() {

    private lateinit var sharedPreferences: SharedPreferencesManager
    private lateinit var biometricsEnabledPreference: SwitchPreferenceCompat

    override fun onResume() {
        super.onResume()
        updateSettings()
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
        sharedPreferences = SharedPreferencesManager.getInstance(requireContext())
        biometricsEnabledPreference = findPreference<SwitchPreferenceCompat>("biometrics")!!

        updateSettings()

        /* Biometrics */
        biometricsEnabledPreference?.setOnPreferenceChangeListener { _, newValue ->
            if (newValue as Boolean) {
                /* enable */
                val intent = Intent(requireContext(), Login::class.java)
                intent.putExtra("STATE", Login.Companion.REGISTER_STATE.ACTIVATE_BIOMETRICS)
                startActivity(intent)
            } else {
                /* disable */
                sharedPreferences.saveString(getString(R.string.shared_prefs_biometrics_key), "")
            }
            true
        }
    }

    private fun updateSettings() {
        biometricsEnabledPreference?.isChecked = ! sharedPreferences.getString(getString(R.string.shared_prefs_biometrics_key), "").isNullOrEmpty()
    }
}