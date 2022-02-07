package com.newcore.wezy.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.newcore.wezy.WeatherApplication
import com.newcore.wezy.databinding.ActivityMainBinding
import com.newcore.wezy.repository.WeatherRepo
import com.newcore.wezy.utils.INetwork

class MainActivity : AppCompatActivity() , INetwork {

    val appStateViewModel by lazy{
        val viewModelFactory = AppStateViewModel.Factory(
            this.applicationContext as WeatherApplication,
            WeatherRepo()
        )
        ViewModelProvider(this,viewModelFactory)[AppStateViewModel::class.java]
    }

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(binding.newsNavHostFragment.id) as NavHostFragment

        binding.bottomNavigationView.setupWithNavController(navHostFragment.navController)
    }

    override fun showNoInternet() {
        binding.tvNoInternetConnection.visibility = View.VISIBLE
    }

    override fun hideNoInternet() {
        binding.tvNoInternetConnection.visibility = View.GONE
    }
}
