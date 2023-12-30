package de.xuuniversity.co3.klobuddy

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import de.xuuniversity.co3.klobuddy.databinding.ActivityMainBinding
import de.xuuniversity.co3.klobuddy.preferences.SettingsFragment

class MainActivity : AppCompatActivity(), FavoritesAdapter.FavoritesAdapterCallback {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)

        val themeModeInt = when (sharedPref.getString("themePref", "System default")) {
            "Light" -> AppCompatDelegate.MODE_NIGHT_NO
            "Dark" -> AppCompatDelegate.MODE_NIGHT_YES
            "System default" -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(themeModeInt)

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomNavigationView.selectedItemId = R.id.action_menu_map


        supportFragmentManager.beginTransaction()
            .replace(R.id.frameLayout, MapsFragment())
            .commit()
        binding.bottomNavigationView.menu.findItem(R.id.action_menu_map).isChecked = true

        binding.bottomNavigationView.setOnItemSelectedListener { item ->

            binding.bottomNavigationView.menu.findItem(R.id.action_menu_map).isEnabled = true
            binding.bottomNavigationView.menu.findItem(R.id.action_menu_favorites).isEnabled = true
            binding.bottomNavigationView.menu.findItem(R.id.action_menu_settings).isEnabled = true

            when (item.itemId) {
                R.id.action_menu_map -> {
                    replaceFragment(MapsFragment())
                    binding.bottomNavigationView.menu.findItem(R.id.action_menu_map).isEnabled =
                        false
                    true
                }

                R.id.action_menu_favorites -> {
                    replaceFragment(FavoritesFragment())
                    binding.bottomNavigationView.menu.findItem(R.id.action_menu_favorites).isEnabled =
                        false
                    true
                }

                R.id.action_menu_settings -> {
                    replaceFragment(SettingsFragment())
                    binding.bottomNavigationView.menu.findItem(R.id.action_menu_settings).isEnabled =
                        false
                    true
                }

                else -> false
            }
        }
    }
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frameLayout, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onNavigateToMap() {
        binding.bottomNavigationView.selectedItemId = R.id.action_menu_map
        replaceFragment(MapsFragment())
    }
}