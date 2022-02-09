package com.newcore.wezy.ui.map

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.newcore.wezy.R
import com.newcore.wezy.WeatherApplication
import com.newcore.wezy.databinding.FragmentMapsBinding
import com.newcore.wezy.ui.BaseFragment

class MapsFragment : BaseFragment<FragmentMapsBinding>(FragmentMapsBinding::inflate) ,OnMapReadyCallback {
    private lateinit var googleMap:GoogleMap;
    private var latLng: LatLng? = null

    private val mapsViewModel by lazy{
        val viewModelFactory = MapsViewModel.Factory(
            requireContext().applicationContext as WeatherApplication
        )
        ViewModelProvider(this,viewModelFactory)[MapsViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    val markers = ArrayList<Marker>()

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap;
        init()


        googleMap.setOnMapClickListener { latLng->
            markers.forEach { it.remove() }
            markers.clear()

            googleMap.addMarker(MarkerOptions().position(latLng))?.let { markers.add(it) }
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))

            this.latLng = latLng
            mapsViewModel.setLocation(latLng, viewModel)
        }
    }



    private fun init(){
        binding.apply {

            viewModel.getSettings().location?.also { location ->
                googleMap.addMarker(MarkerOptions().position(location.latLng))?.let { markers.add(it) }
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(location.latLng))

                binding.tvCountry.text = location.country?:""
                binding.tvLocationLine.text = location.locationName?:""
            }


            mapsViewModel.locationMutableLiveData.observe(viewLifecycleOwner) { mlocation->
                mlocation.also{
                    tvCountry.text = it.country?:getString(R.string.cantFindCountyName)
                    tvLocationLine.text = it.locationName?:""
                }
            }

            btnSelectLocation.setOnClickListener {
                if(latLng!=null){
                    viewModel.updateSettingsLocation(latLng!!)
                }
                findNavController().popBackStack()
            }
        }

    }

}