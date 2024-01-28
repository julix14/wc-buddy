package de.xuuniversity.co3.klobuddy

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.google.firebase.auth.FirebaseAuth
import de.xuuniversity.co3.klobuddy.databinding.ActivityMainBinding
import de.xuuniversity.co3.klobuddy.favorite.FavoritesAdapter
import de.xuuniversity.co3.klobuddy.favorite.FavoritesFragment
import de.xuuniversity.co3.klobuddy.preferences.SettingsFragment
import de.xuuniversity.co3.klobuddy.singletons.StatesSingleton
import de.xuuniversity.co3.klobuddy.wc.WcRepository
import kotlinx.coroutines.launch

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
            StatesSingleton.userId = FirebaseAuth.getInstance().currentUser?.uid?.hashCode() ?: 1
            startActivity(intent)
            finish()
        } else {
            StatesSingleton.userId = FirebaseAuth.getInstance().currentUser?.uid?.hashCode() ?: 1
        }
        setContentView(binding.root)
        Log.d("Login", "User ID: ${StatesSingleton.userId}")

        lifecycleScope.launch {
            WcRepository.upsertWcEntitiesFromFireStore(this@MainActivity)
            WcRepository.upsertUserFavoritesFromFireStore(this@MainActivity)
        }


        binding.bottomNavigationView.selectedItemId = R.id.action_menu_map


        supportFragmentManager.beginTransaction()
            .replace(R.id.frameLayout, MapsFragment())
            .addToBackStack(MapsFragment().javaClass.simpleName)
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
        val callback = object : OnBackPressedCallback(true /* enabled by default */) {
            override fun handleOnBackPressed() {
                Log.d("MainActivity", "Back pressed")
                if (supportFragmentManager.backStackEntryCount < 2) {
                    finish()
                    return
                }
                val fragmentToNavigate =
                    supportFragmentManager.getBackStackEntryAt(supportFragmentManager.backStackEntryCount - 2).name.toString()
                supportFragmentManager.popBackStack()

                when (fragmentToNavigate) {
                    "MapsFragment" -> {
                        binding.bottomNavigationView.selectedItemId = R.id.action_menu_map
                        replaceFragment(MapsFragment(), false)
                    }

                    "FavoritesFragment" -> {
                        binding.bottomNavigationView.selectedItemId = R.id.action_menu_favorites
                        replaceFragment(FavoritesFragment(), false)

                    }

                    "SettingsFragment" -> {
                        binding.bottomNavigationView.selectedItemId = R.id.action_menu_settings
                        replaceFragment(SettingsFragment(), false)

                    }
                }


            }
        }
        onBackPressedDispatcher.addCallback(this, callback)
    }

    private fun replaceFragment(fragment: Fragment, addToBackStack: Boolean = true) {
        if (addToBackStack) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, fragment)
                .addToBackStack(fragment.javaClass.simpleName)
                .commit()
        } else {
            supportFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, fragment)
                .commit()
        }
    }

    override fun onNavigateToMap() {
        binding.bottomNavigationView.selectedItemId = R.id.action_menu_map
        replaceFragment(MapsFragment())
    }
}