package de.xuuniversity.co3.klobuddy.fragments

import android.app.AlertDialog
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.firebase.auth.FirebaseAuth
import de.xuuniversity.co3.klobuddy.R

class SettingsFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener {
    private var isDialogShown = false

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

            // Save the theme preference
            val sharedPref = preferenceManager.sharedPreferences
            with(sharedPref.edit()) {
                putString("themePref", themeMode.toString())
                apply()
            }
            true


        }
        val signOutPreference: Preference? = findPreference("sign_out_preference")
        signOutPreference?.setOnPreferenceClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.sign_out_dialog_title)
                .setMessage(R.string.sign_out_dialog_message)
                .setPositiveButton(R.string.dialog_option_yes) { _, _ ->
                    FirebaseAuth.getInstance().signOut()
                    activity?.finish()
                }
                .setNegativeButton(R.string.dialog_option_no) { dialog, _ ->
                    dialog.dismiss()
                    isDialogShown = false
                }
                .show()

            true
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == "themePref" && !isDialogShown) {
            isDialogShown = true
            // Show an alert dialog
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.theme_change_dialog_title)
                .setMessage(R.string.theme_change_dialog_message)
                .setPositiveButton(R.string.dialog_option_restart) { _, _ ->
                    // Restart the app
                    activity?.finish()
                }
                .setNegativeButton(R.string.dialog_option_later) { dialog, _ ->
                    dialog.dismiss()
                    isDialogShown = false
                }
                .show()
        }


    }

    override fun onResume() {
        super.onResume()
        // Register this fragment as a SharedPreferences change listener
        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        // Unregister this fragment as a SharedPreferences change listener
        preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }


}