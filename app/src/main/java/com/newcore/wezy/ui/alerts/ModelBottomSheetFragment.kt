package com.newcore.wezy.ui.alerts

import android.app.AlertDialog
import android.app.ProgressDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RadioGroup
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.newcore.wezy.R
import com.newcore.wezy.WeatherApplication
import com.newcore.wezy.databinding.IncludeAddAlertBottomSheetBinding
import com.newcore.wezy.localDb.WeatherDatabase
import com.newcore.wezy.models.MyAlert
import com.newcore.wezy.repository.WeatherRepo
import com.newcore.wezy.shareprefrances.SettingsPreferences
import com.newcore.wezy.utils.ViewHelpers
import kotlinx.coroutines.launch
import okhttp3.internal.notify
import java.util.*

private fun Calendar.setHourMinute(hourOfDay: Int, minute: Int): Calendar {
    this.set(Calendar.HOUR_OF_DAY, hourOfDay)
    this.set(Calendar.MINUTE, minute)
    return this
}

class ModelBottomSheetFragment : BottomSheetDialogFragment(),
    View.OnClickListener, RadioGroup.OnCheckedChangeListener {

    val settings by lazy {
        SettingsPreferences(requireContext().applicationContext as WeatherApplication)
            .get()
    }

    val binding by lazy {
        IncludeAddAlertBottomSheetBinding.inflate(layoutInflater)
    }


    private val alertsViewModel by lazy {
        ViewModelProvider(
            this, AlertsViewModel.Factory(
                requireContext().applicationContext as WeatherApplication,
                WeatherRepo(
                    SettingsPreferences(requireContext().applicationContext as WeatherApplication),
                    WeatherDatabase(requireContext()),
                )
            )
        )[AlertsViewModel::class.java]
    }

    private val modelBottomViewModel by lazy {
        ViewModelProvider(
            this, ModelAlertsViewModel.Factory(
                requireContext().applicationContext as WeatherApplication,
                WeatherRepo(
                    SettingsPreferences(requireContext().applicationContext as WeatherApplication),
                    WeatherDatabase(requireContext()),
                )
            )
        )[ModelAlertsViewModel::class.java]
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {

            etFromTime.setOnClickListener(::onClick)
            etToTime.setOnClickListener(::onClick)

            etFromTime.addTextChangedListener {
                validateForm()
            }

            etToTime.addTextChangedListener{
                validateForm()
            }

            rgInformWith.check(binding.rbAlert.id)

            rgInformWith.setOnCheckedChangeListener(::onCheckedChanged)

            btnSave.setOnClickListener {
                if(!validateForm())
                    return@setOnClickListener

                lifecycleScope.launch {
                    showLoading("adding alert")
                    alertsViewModel.addAlert(
                        modelBottomViewModel.myAlert.copy(
                            lat = settings.location?.latLng?.latitude,
                            lon = settings.location?.latLng?.longitude
                        )
                    )
                    hideLoading()
                    this@ModelBottomSheetFragment.dismiss()
                }

            }

            btnCancel.setOnClickListener {
                this@ModelBottomSheetFragment.dismiss()
            }



        }
    }

    private fun validateForm():Boolean{
        var isValid = true;
        binding.apply {

            etLayoutFromTime.apply {
                if(Date(modelBottomViewModel.myAlert.fromDT) < Date()){
                    error = "Start time must be in the future"
                    isValid = false
                }else{
                    binding.etFromTime.error = null
                    error = null
                }
            }

            etLayoutToTime.apply {
                if(Date(modelBottomViewModel.myAlert.toDT) < Date())
                {
                    error = "End time must be in the future"
                    isValid = false
                }else if(Date(modelBottomViewModel.myAlert.toDT) <= Date(modelBottomViewModel.myAlert.fromDT)){
                    error = "End time must be after Start Time"
                    isValid = false
                }else{
                    binding.etToTime.error = null
                    error = null
                }
            }
        }
        return isValid
    }

    override fun onClick(p0: View?) {
        val et = p0 as EditText

        val currentTime: Calendar = Calendar.getInstance()
        when(et.id){
            R.id.etFromTime->
                currentTime.timeInMillis = modelBottomViewModel.myAlert.fromDT
            R.id.etToTime->
                currentTime.timeInMillis = modelBottomViewModel.myAlert.toDT
        }

        val hour: Int = currentTime.get(Calendar.HOUR_OF_DAY)
        val minute: Int = currentTime.get(Calendar.MINUTE)

        val mTimePicker = TimePickerDialog(
            requireContext(),
            { timepicker, selectedHour, selectedMinute ->
                val time = Calendar.getInstance()
                    .setHourMinute(selectedHour, selectedMinute)
                when (et.id) {
                    R.id.etFromTime ->
                        modelBottomViewModel.myAlert.fromDT = time.timeInMillis
                    R.id.etToTime ->
                        modelBottomViewModel.myAlert.toDT = time.timeInMillis
                }

                et.setText(
                    ViewHelpers.getTimeFromDate(
                        time.time,
                        settings.language
                    )
                )
            },
            hour,
            minute,
            false
        )

        mTimePicker.show()
    }

    override fun onCheckedChanged(p0: RadioGroup?, p1: Int) {
        modelBottomViewModel.myAlert.isAlarm = p1 == binding.rbAlert.id
    }

    var dialog: AlertDialog? = null
     fun showLoading(message:String?) {
        dialog = ProgressDialog.show(activity, "",message?:"Loading. Please wait...", true);
    }

     fun hideLoading() {
        dialog?.dismiss()
    }

}