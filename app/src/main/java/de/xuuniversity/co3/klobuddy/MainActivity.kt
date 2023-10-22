package de.xuuniversity.co3.klobuddy

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import de.xuuniversity.co3.klobuddy.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportFragmentManager.beginTransaction()
            .replace(R.id.frameLayout, MapsFragment())
            .commit()
        binding.bottomNavigationView.menu.findItem(R.id.action_menu_map).isChecked = true

        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_menu_map -> {
                    replaceFragment(MapsFragment())
                    true
                }

                R.id.action_menu_favorites -> {
                    replaceFragment(FavoritesFragment())
                    true
                }

                R.id.action_menu_profile -> {
                    replaceFragment(ProfileFragment())
                    true
                }

                else -> false
            }
        }
    }
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frameLayout, fragment)
            .commit()
    }
}