/*
package com.example.harmonycare.login

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.harmonycare.R
import com.example.harmonycare.databinding.ActivityLoginTestBinding
import com.example.harmonycare.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.signin.GoogleSignInOptions.*

class LoginTestActivity : AppCompatActivity(), DataBinding<ActivityLoginTestBinding> {

    override val binding by lazy { DataBinding.get<ActivityLoginTestBinding>(this,
        R.layout.activity_login_test
    ) }

    private val navController by lazy {
        (supportFragmentManager.findFragmentById(R.id.fragment) as NavHostFragment).navController
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onCreateDataBinding()
        onCreateSupportActionBar()
    }

    private fun onCreateDataBinding() {

        binding.lifecycleOwner = this
    }

    private fun onCreateSupportActionBar() {
        setSupportActionBar(binding.toolbar)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun setSupportActionBar(toolbar: Toolbar?) {
        super.setSupportActionBar(toolbar)
        setupActionBarWithNavController(navController)
    }
}
*/
