package com.newcore.wezy.ui.settings

import android.os.Bundle
import android.view.View
import android.widget.RadioGroup
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.newcore.wezy.R
import com.newcore.wezy.databinding.FragmentSettingsBinding
import com.demo.data.shareprefrances.*
import com.newcore.wezy.ui.BaseFragment
import com.demo.core.utils.Resource


class SettingsFragment
    : BaseFragment<FragmentSettingsBinding>(FragmentSettingsBinding::inflate)  {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSettings()

    }

    private fun onCheckedChanged(radioGroup: RadioGroup?, i: Int) {
        binding.apply {
            viewModel.updateSettings {
                it.copy().apply {
                    when (radioGroup) {
                        rgLanguage -> language =
                            when (i) {
                                rbArabic.id -> {
                                    setAppLocale("ar")
                                    Language.Arabic
                                }
                                rbEnglish.id -> {
                                    setAppLocale("en")
                                    Language.English
                                }
                                rbDefaultLanguage.id->{
                                    setAppLocale()
                                    Language.Default
                                }
                                else -> Language.Default
                            }
                        rgLocation -> defineLocationType =
                            when (i) {
                                rbGps.id -> {
                                    DefineLocationType.Gps
                                }
                                rbMap.id -> {

                                    DefineLocationType.Maps
                                }
                                else -> {
                                    DefineLocationType.Maps
                                }
                            }

                        rgNotification -> notificationState =
                            when (i) {
                                rbEnable.id -> NotificationState.Enable
                                rbDisable.id -> NotificationState.Disable
                                else -> NotificationState.Enable
                            }

                        rgTempUnit -> tempUnit =
                            when (i) {
                                rbFahrenheit.id -> TempUnit.Fahrenheit
                                rbCelsius.id -> TempUnit.Celsius
                                rbKelvin.id -> TempUnit.Kelvin
                                else -> TempUnit.Fahrenheit
                            }

                        rgWindSpeedUnit -> windSpeedUnit =
                            when (i) {
                                rbMBS.id -> WindSpeedUnit.MeterBerSecond
                                rbMBH.id -> WindSpeedUnit.MileBerHour
                                else -> WindSpeedUnit.MeterBerSecond
                            }
                    }
                }
            }
        }
    }



    private fun setupSettings() {
        binding.apply {
            viewModel.getSettings().also {
                rgLanguage.check(
                    when (it.language) {
                        Language.Arabic -> rbArabic
                        Language.English -> rbEnglish
                        Language.Default -> rbDefaultLanguage
                    }.id
                )

                rgLocation.check(
                    when (it.defineLocationType) {
                        DefineLocationType.Gps -> rbGps
                        DefineLocationType.Maps -> rbMap
                    }.id
                )

                rgNotification.check(
                    when (it.notificationState) {
                        NotificationState.Enable -> rbEnable
                        NotificationState.Disable -> rbDisable
                    }.id
                )

                rgTempUnit.check(
                    when (it.tempUnit) {
                        TempUnit.Fahrenheit -> rbFahrenheit
                        TempUnit.Kelvin -> rbKelvin
                        TempUnit.Celsius -> rbCelsius
                    }.id
                )

                rgWindSpeedUnit.check(
                    when (it.windSpeedUnit) {
                        WindSpeedUnit.MeterBerSecond -> rbMBS
                        WindSpeedUnit.MileBerHour -> rbMBH
                    }.id
                )
            }

            rgLanguage.setOnCheckedChangeListener(::onCheckedChanged)
            rgLocation.setOnCheckedChangeListener(::onCheckedChanged)
            rgNotification.setOnCheckedChangeListener(::onCheckedChanged)
            rgTempUnit.setOnCheckedChangeListener(::onCheckedChanged)
            rgWindSpeedUnit.setOnCheckedChangeListener(::onCheckedChanged)


            rbGps.setOnClickListener {
                viewModel.locationPermissionMutableLiveData
                    .observe(viewLifecycleOwner,object:Observer<Resource<Int>>{
                        override fun onChanged(permissionState: Resource<Int>?) {
                            when(permissionState){
                                is Resource.Error -> {
                                    println("location.gps.error")
                                    hideLoading()
                                    viewModel.locationPermissionMutableLiveData.removeObserver(this)
                                    viewModel.locationPermissionMutableLiveData = MutableLiveData()
                                    rgLocation.check(rbMap.id)
                                }
                                is Resource.Loading -> {
                                    println("location.gps.Loading")

                                    showLoading()
                                }
                                is Resource.Success -> {
                                    println("location.gps.Success")
                                    hideLoading()
                                    viewModel.locationPermissionMutableLiveData.removeObserver(this)
                                    viewModel.locationPermissionMutableLiveData = MutableLiveData()
                                    rgLocation.check(rbGps.id)
                                }
                                null -> {
                                    println("location.gps.null")
                                }
                            }
                        }

                    })

                getLocationWithGps()

            }

            rbMap.setOnClickListener {
                findNavController()
                    .navigate(R.id.action_settingsFragment_to_mapsFragment)
            }


        }
    }



}