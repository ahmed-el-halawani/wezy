package com.newcore.wezy.ui.settings

import android.os.Bundle
import android.view.View
import android.widget.RadioGroup
import com.newcore.wezy.databinding.FragmentSettingsBinding
import com.newcore.wezy.shareprefrances.*
import com.newcore.wezy.ui.BaseFragment
import com.newcore.wezy.utils.ViewHelpers


class SettingsFragment
    : BaseFragment<FragmentSettingsBinding>(FragmentSettingsBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSettings()

    }

    private fun setAppLocale(localeCode: String) =
        ViewHelpers.setAppLocale(localeCode, resources, requireActivity())

    private fun onCheckedChanged(radioGroup: RadioGroup?, i: Int) {
        binding.apply {
            viewModel.settings =
                viewModel.settings.apply {
                    language =
                        when (i) {
                            rbArabic.id -> {
                                setAppLocale("ar")
                                Language.Arabic
                            }
                            rbEnglish.id -> {
                                setAppLocale("en")
                                Language.English
                            }
                            else -> Language.Arabic
                        }

                    defineLocationType =
                        when (i) {
                            rbGps.id -> DefineLocationType.Gps
                            rbMap.id -> DefineLocationType.Maps
                            else -> DefineLocationType.Gps
                        }

                    notificationState =
                        when (i) {
                            rbEnable.id -> NotificationState.Enable
                            rbDisable.id -> NotificationState.Disable
                            else -> NotificationState.Enable
                        }

                    tempUnit =
                        when (i) {
                            rbFahrenheit.id -> TempUnit.Fahrenheit
                            rbCelsius.id -> TempUnit.Celsius
                            rbKelvin.id -> TempUnit.Kelvin
                            else -> TempUnit.Fahrenheit
                        }

                    windSpeedUnit =
                        when (i) {
                            rbMBS.id -> WindSpeedUnit.MeterBerSecond
                            rbMBH.id -> WindSpeedUnit.MileBerHour
                            else -> WindSpeedUnit.MeterBerSecond
                        }
                }
        }
    }

    private fun setupSettings() {
        binding.apply {

            rgLanguage.setOnCheckedChangeListener(::onCheckedChanged)
            rgLocation.setOnCheckedChangeListener(::onCheckedChanged)
            rgNotification.setOnCheckedChangeListener(::onCheckedChanged)
            rgTempUnit.setOnCheckedChangeListener(::onCheckedChanged)
            rgWindSpeedUnit.setOnCheckedChangeListener(::onCheckedChanged)

            viewModel.settings.also {
                rgLanguage.check(
                    when (it.language) {
                        Language.Arabic -> rbArabic
                        Language.English -> rbEnglish
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

        }
    }

}