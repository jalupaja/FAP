package com.example.fap.ui.settings

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.BuildCompat
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.example.fap.Login
import com.example.fap.R
import com.example.fap.utils.SharedPreferencesManager
import com.example.fap.utils.SharedSecurityManager

class SettingsFragment : PreferenceFragmentCompat() {

    private lateinit var sharedPreferences: SharedPreferencesManager
    private lateinit var sharedSecurity: SharedSecurityManager
    private lateinit var biometrics: SwitchPreferenceCompat
    private lateinit var theme: ListPreference

    override fun onResume() {
        super.onResume()
        updateSettings()
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
        sharedPreferences = SharedPreferencesManager.getInstance(requireContext())
        sharedSecurity = SharedSecurityManager.getInstance(requireContext())
        biometrics = findPreference<SwitchPreferenceCompat>("biometrics")!!
        theme = findPreference<ListPreference>("theme")!!

        updateSettings()

        /* Biometrics */
        biometrics.setOnPreferenceChangeListener { _, newValue ->
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

        /* Theme */
        theme.setOnPreferenceChangeListener { preference, newValue ->
            newValue as? String
            when (newValue) {
                getString(R.string.theme_light) -> {
                    updateTheme(AppCompatDelegate.MODE_NIGHT_NO)
                }
                getString(R.string.theme_dark) -> {
                    updateTheme(AppCompatDelegate.MODE_NIGHT_YES)
                }
                getString(R.string.theme_oled) -> {
                    /* TODO Add Oled theme
                        updateTheme() */
                }
                else -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                }
            }
            true
        }
    }

    private fun updateSettings() {
        biometrics.isChecked = ! sharedPreferences.getString(getString(R.string.shared_prefs_biometrics_key)).isNullOrEmpty()
        biometrics.isEnabled = sharedSecurity.checkBiometric()
    }

    private fun updateTheme(mode: Int) {
        sharedPreferences.saveInt(getString(R.string.shared_prefs_theme), mode)
        AppCompatDelegate.setDefaultNightMode(mode)
        requireActivity().recreate()
    }
}