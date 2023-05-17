package com.example.fap.ui.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.example.fap.Login
//import com.example.fap.BiometricHandler
import com.example.fap.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onResume() {
        super.onResume()
        val biometricsEnabledPreference = findPreference<SwitchPreferenceCompat>("biometrics")
        val sharedPreferences = context?.getSharedPreferences(getString(R.string.shared_prefs), Context.MODE_PRIVATE)

        biometricsEnabledPreference?.isChecked = !sharedPreferences?.getString(getString(R.string.shared_prefs_biometrics_key), "").isNullOrEmpty()
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        val biometricsEnabledPreference = findPreference<SwitchPreferenceCompat>("biometrics")
        biometricsEnabledPreference?.setOnPreferenceChangeListener { _, newValue ->
            if (newValue as Boolean) {
                /* TODO enable biometrics */
                val intent = Intent(requireContext(), Login::class.java)
                intent.putExtra("STATE", Login.Companion.REGISTER_STATE.ACTIVATE_BIOMETRICS)
                startActivity(intent)

            } else {
                val editor = context?.getSharedPreferences(getString(R.string.shared_prefs), Context.MODE_PRIVATE)?.edit()
                if (editor != null) {
                    editor.putString(getString(R.string.shared_prefs_biometrics_key), "")
                }
            }
            true
        }
    }
}