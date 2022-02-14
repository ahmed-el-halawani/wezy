package com.newcore.wezy.ui.searchNews

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.material.snackbar.Snackbar
import com.newcore.wezy.WeatherApplication
import com.newcore.wezy.databinding.FragmentAlertsBinding
import com.newcore.wezy.localDb.WeatherDatabase
import com.newcore.wezy.repository.WeatherRepo
import com.newcore.wezy.services.LongRunningWorker
import com.newcore.wezy.shareprefrances.SettingsPreferences
import com.newcore.wezy.ui.BaseFragment
import com.newcore.wezy.utils.Constants.MY_ALERT_ID
import com.newcore.wezy.utils.Constants.MY_ALERT_LAT
import com.newcore.wezy.utils.Constants.MY_ALERT_LNG
import com.newcore.wezy.utils.Constants.MY_ALERT_TO
import com.newcore.wezy.utils.ViewHelpers
import kotlinx.coroutines.launch
import java.time.Duration


class AlertsFragment
    :BaseFragment<FragmentAlertsBinding>( FragmentAlertsBinding::inflate ) {


    private val alertAdapter by lazy {
        AlertsAdapter().apply {
            setOnItemClickListener { }
        }
    }




    var afterPermissionFun:(()->Unit)? = null

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        alertsViewModel.loadingLiveData.observe(viewLifecycleOwner){
            if(it)
                showLoading("adding your alert")
            else
                hideLoading()

        }

        mainActivity.activityResultLiveData.observe(viewLifecycleOwner){
            requestOverLayPermission(afterPermissionFun);
        }


        setupRecycleView()

        ItemTouchHelper(ViewHelpers.SwipeToRemove { position ->
            val alert = alertAdapter.differ.currentList[position]

            //delete article
            alertsViewModel.deleteAlert(alert)

            // set undo option
            showSnackbar2("remove location done successfully") {
                lifecycleScope.launch {
                    alertsViewModel.addAlert(alert)
                }
            }
        })
            .attachToRecyclerView(binding.rvAlerts)

        // observe to live data
        alertsViewModel.getData().observe(viewLifecycleOwner) { alerts ->
            alertAdapter.differ.submitList(alerts)
        }

        binding.floatingActionButton.setOnClickListener {
            requestOverLayPermission(::showBottomSheetDialog)
        }

    }

    fun showBottomSheetDialog() {
        val bottomSheetFragment = ModelBottomSheetFragment()
        bottomSheetFragment.show(childFragmentManager, bottomSheetFragment.tag)
    }


    private fun setupRecycleView() {
        binding.rvAlerts.apply {
            adapter = alertAdapter
            layoutManager = LinearLayoutManager(activity)
        }
    }

    fun showSnackbar2(message: String?=null, undoAction: View.OnClickListener) {
        Snackbar.make(binding.root, message ?: "", Snackbar.LENGTH_LONG).apply {
            setAction("UNDO", undoAction)
            show()
        }
    }

    fun requestOverLayPermission(afterPermission:(()->Unit)?){
        @Suppress("DEPRECATION")
        if (!Settings.canDrawOverlays(context)) {
            afterPermissionFun = afterPermission

            val intent =
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context?.packageName}"))
            startActivityForResult(intent, 500)
        }else{
            (afterPermission?:afterPermissionFun)?.invoke()
        }
    }


    fun getDateTime(){

    }


    fun setupWorker(){

        val data = Data.Builder()

        data.putInt(MY_ALERT_ID,1)
        data.putDouble(MY_ALERT_LAT,100.0)
        data.putDouble(MY_ALERT_LNG,300.0)
        data.putLong(MY_ALERT_TO,100)

        val request = OneTimeWorkRequestBuilder<LongRunningWorker>()
            .setInitialDelay(Duration.ofMillis(10000L))
            .setInputData(data.build())
            .build()

        WorkManager.getInstance(requireContext())
            .enqueue(request)
    }



}