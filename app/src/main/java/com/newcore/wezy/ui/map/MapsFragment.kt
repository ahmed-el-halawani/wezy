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
import com.newcore.wezy.ui.MainActivity
import com.demo.core.utils.Resource
import com.demo.data.shareprefrances.DefineLocationType

open class MapsFragment : BaseFragment<FragmentMapsBinding>(FragmentMapsBinding::inflate) ,OnMapReadyCallback {
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
        (activity as MainActivity).setSupportActionBar(binding.toolbar)
        onBackd()
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


    open fun initDataWithSettings(){

        viewModel.getSettings().location.also { location ->
            location?.let {
                googleMap.addMarker(MarkerOptions().position(location.latLng))?.let { markers.add(it) }
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(location.latLng))
            }

            if(location?.latLng==null)
            {
                binding.tvCountry.text = getString(R.string.locatoin_not_selected)
                binding.tvLocationLine.text = ""
                binding.btnSelectLocation.visibility = View.GONE
            }else{
                binding.tvCountry.text = location.country ?:""
                binding.tvLocationLine.text = location.locationName ?:""
                binding.btnSelectLocation.visibility = View.VISIBLE
            }

        }
    }


    private fun init(){
        binding.apply {

            initDataWithSettings()

            mapsViewModel.locationMutableLiveData.observe(viewLifecycleOwner) { locationResource->

                    btnSelectLocation.visibility = View.VISIBLE

                    when(locationResource){
                        is Resource.Error -> pbLoading.visibility = View.INVISIBLE
                        is Resource.Loading -> {
                            pbLoading.visibility = View.VISIBLE
                            tvCountry.text = ""
                            tvLocationLine.text = ""
                        }
                        is Resource.Success -> locationResource.data?.apply {
                            pbLoading.visibility = View.INVISIBLE
                            tvCountry.text = country?:getString(R.string.cantFindCountyName)
                            tvLocationLine.text = locationName?:""
                        }
                    }

            }

            btnSelectLocation.setOnClickListener {
                onButtonClick(latLng)
            }
        }

    }

    open fun onButtonClick(latLng:LatLng?){
        if(latLng!=null){
            viewModel.updateSettingsLocation(latLng,DefineLocationType.Maps)
        }
        findNavController().popBackStack()
    }

    private fun onBackd() {
        mainActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

}