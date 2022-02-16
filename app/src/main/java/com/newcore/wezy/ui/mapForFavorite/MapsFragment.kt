package com.newcore.wezy.ui.map

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.navigateUp
import com.google.android.gms.maps.*

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.newcore.wezy.R
import com.newcore.wezy.WeatherApplication
import com.newcore.wezy.databinding.FragmentMapsBinding
import com.newcore.wezy.ui.BaseFragment
import com.newcore.wezy.ui.MainActivity
import com.newcore.wezy.utils.Resource

class MapsForFavoriteFragment : MapsFragment() {

    override fun initDataWithSettings() {
        binding.tvCountry.text = getString(R.string.locatoin_not_selected)
        binding.tvLocationLine.text = ""
        binding.btnSelectLocation.visibility = View.GONE
    }

    override fun onButtonClick(latLng: LatLng?) {
        findNavController().apply {
            previousBackStackEntry?.savedStateHandle?.set("latLng", latLng)
            popBackStack()
        }
    }
}