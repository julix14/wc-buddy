package de.xuuniversity.co3.klobuddy

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
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
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, MapsFragment())
                        .commit()
                    true
                }

                R.id.action_menu_favorites -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, FavoritesFragment())
                        .commit()
                    true
                }

                R.id.action_menu_profile -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, ProfileFragment())
                        .commit()
                    true
                }

                else -> false
            }
        }
    }
}