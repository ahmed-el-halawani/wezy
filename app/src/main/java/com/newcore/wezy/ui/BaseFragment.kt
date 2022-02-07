package com.newcore.wezy.ui

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.newcore.wezy.WeatherApplication
import com.newcore.wezy.shareprefrances.SettingsPreferences
import com.newcore.wezy.utils.INetwork

abstract class BaseFragment<T : ViewBinding>(val viewBindingInflater:(LayoutInflater)->T): Fragment() {

    val  viewModel by lazy {
        (activity as MainActivity).appStateViewModel
    }

    val networkState by lazy{
        activity as INetwork
    }

    val binding by lazy {
        viewBindingInflater(layoutInflater)
    }


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            return binding.root
    }

}