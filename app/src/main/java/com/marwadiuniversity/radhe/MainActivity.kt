package com.marwadiuniversity.radhe

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.marwadiuniversity.radhe.databinding.ActivityMainBinding
import com.marwadiuniversity.radhe.fragments.CustomersFragment
import com.marwadiuniversity.radhe.fragments.HomeFragment
import com.marwadiuniversity.radhe.fragments.ProductsFragment
import com.marwadiuniversity.radhe.fragments.ReportsFragment
import com.marwadiuniversity.radhe.fragments.SettingsFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> loadFragment(HomeFragment())
                R.id.navigation_customers -> loadFragment(CustomersFragment())
                R.id.navigation_products -> loadFragment(ProductsFragment())
                R.id.navigation_reports -> loadFragment(ReportsFragment())
                R.id.navigation_settings -> loadFragment(SettingsFragment())
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment): Boolean {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
        return true
    }
}
