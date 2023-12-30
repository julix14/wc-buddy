package de.xuuniversity.co3.klobuddy

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import de.xuuniversity.co3.klobuddy.databinding.ActivityMainBinding
import de.xuuniversity.co3.klobuddy.preferences.SettingsFragment
import de.xuuniversity.co3.klobuddy.singletons.StatesSingleton

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
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            // User not logged in, redirect to LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            StatesSingleton.userId = FirebaseAuth.getInstance().currentUser?.uid?.toInt() ?: 1
            startActivity(intent)
            finish()
        }
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
        replaceFragment(MapsFragment())
    }
}