package com.example.harmonycare

import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.harmonycare.data.SharedPreferencesManager
import com.example.harmonycare.databinding.ActivityMainBinding
import com.example.harmonycare.retrofit.ApiManager
import com.example.harmonycare.retrofit.ApiService
import com.example.harmonycare.retrofit.RetrofitClient
import com.example.harmonycare.ui.checklist.ChecklistFragment
import com.example.harmonycare.ui.record.RecordFragment
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        navController = findNavController(R.id.nav_host_fragment_activity_main)

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_checklist, R.id.navigation_record, R.id.navigation_pattern,R.id.navigation_community,R.id.navigation_profile
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        navView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_checklist, R.id.navigation_record, R.id.navigation_pattern, R.id.navigation_community, R.id.navigation_profile -> {
                    if (navController.currentDestination?.id != item.itemId) {
                        navController.navigate(item.itemId)
                    }
                    true
                }
                else -> false
            }
        }
    }

}