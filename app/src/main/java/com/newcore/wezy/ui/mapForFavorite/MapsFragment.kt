package com.newcore.wezy.ui.map

import android.view.View
import androidx.navigation.fragment.findNavController

import com.google.android.gms.maps.model.LatLng
import com.newcore.wezy.R

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