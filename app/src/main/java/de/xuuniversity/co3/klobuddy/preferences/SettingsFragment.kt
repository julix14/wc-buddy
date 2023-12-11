package de.xuuniversity.co3.klobuddy.preferences

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import de.xuuniversity.co3.klobuddy.R

class SettingsFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        val themePreference = findPreference<ListPreference>("themePref")
        themePreference?.setOnPreferenceChangeListener { _, newValue ->
            val themeMode = when (newValue) {
                "Light" -> AppCompatDelegate.MODE_NIGHT_NO
                "Dark" -> AppCompatDelegate.MODE_NIGHT_YES
                "System default" -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }

            Log.d("SettingsFragment", "Theme changed to $newValue, $themeMode")
            AppCompatDelegate.setDefaultNightMode(themeMode)

            // Save the theme preference
            val sharedPref = preferenceManager.sharedPreferences
            with(sharedPref.edit()) {
                putString("themePref", themeMode.toString())
                apply()
            }
            Log.d("SettingsFragment", "Theme changed to $newValue, $sharedPref")
            true


        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        // TODO: Implement
    }
}