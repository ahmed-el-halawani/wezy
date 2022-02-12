package com.newcore.wezy.ui.favorites

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import com.newcore.wezy.R
import com.newcore.wezy.WeatherApplication
import com.newcore.wezy.databinding.FragmentFavoriteBinding
import com.newcore.wezy.localDb.WeatherDatabase
import com.newcore.wezy.repository.WeatherRepo
import com.newcore.wezy.shareprefrances.SettingsPreferences
import com.newcore.wezy.ui.BaseFragment
import com.newcore.wezy.utils.Constants.HOME_WEATHER_ID
import com.newcore.wezy.utils.Extensions.where
import com.newcore.wezy.utils.ILoading
import com.newcore.wezy.utils.ViewHelpers

class FavoritesFragment
    : BaseFragment<FragmentFavoriteBinding>(FragmentFavoriteBinding::inflate) {

    private val favoritesAdapter by lazy {
        FavoritesAdapter().apply {
            setOnItemClickListener {
                val b = Bundle()
                b.putSerializable("location",it)
                findNavController().navigate(R.id.action_favoriteScreen_to_locationPreviewFragment,b)
            }
        }
    }

    private val favoritesViewModel by lazy {
        ViewModelProvider(
            this, FavoritesViewModel.Factory(
                requireContext().applicationContext as WeatherApplication,
                WeatherRepo(
                    SettingsPreferences(requireContext().applicationContext as WeatherApplication),
                    WeatherDatabase(requireContext()),
                ),
                viewModel
            )
        )[FavoritesViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//
        setupRecycleView()

        ItemTouchHelper(ViewHelpers.SwipeToRemove { position ->
            val weatherLang = favoritesAdapter.differ.currentList[position]

            //delete article
            favoritesViewModel.deleteWeatherLang(weatherLang)

            // set undo option
            showSnackbar2("remove location done successfully") {
                favoritesViewModel.addWeatherLang(weatherLang)
            }
        })
            .attachToRecyclerView(binding.rvSavedNews)

        // observe to live data
        favoritesViewModel.getData().observe(viewLifecycleOwner) { weatherList ->
            favoritesAdapter.differ.submitList(weatherList
                .filter { it.id != HOME_WEATHER_ID }.sortedBy { it.id })
        }

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<LatLng?>("latLng")
            ?.observe(viewLifecycleOwner) {
                if(it!=null)
                {
                    favoritesViewModel.createWeather(it)
                    findNavController().currentBackStackEntry?.savedStateHandle?.set("latLng", null)
                }
        }

        binding.floatingActionButton.setOnClickListener {
            findNavController().navigate(R.id.action_favoriteScreen_to_mapsForFavoriteFragment)
        }
    }

    private fun setupRecycleView() {
        binding.rvSavedNews.apply {
            adapter = favoritesAdapter
            layoutManager = LinearLayoutManager(activity)
        }
    }

    fun showSnackbar2(message: String?=null, undoAction: View.OnClickListener) {
        Snackbar.make(binding.root, message ?: "", Snackbar.LENGTH_LONG).apply {
            setAction("UNDO", undoAction)
            show()
        }
    }


    override fun showLoading(message: String?) {
    }

    override fun hideLoading() {
    }





}