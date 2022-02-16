package com.newcore.wezy.ui.alerts

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.newcore.wezy.R
import com.newcore.wezy.WeatherApplication
import com.newcore.wezy.databinding.FragmentAlertsBinding
import com.newcore.wezy.localDb.WeatherDatabase
import com.newcore.wezy.repository.WeatherRepo
import com.newcore.wezy.shareprefrances.SettingsPreferences
import com.newcore.wezy.ui.BaseFragment
import com.newcore.wezy.utils.ViewHelpers
import kotlinx.coroutines.launch


class AlertsFragment
    : BaseFragment<FragmentAlertsBinding>(FragmentAlertsBinding::inflate) {


    private val alertAdapter by lazy {
        AlertsAdapter().apply {
            setOnItemClickListener { }
        }
    }


    var afterPermissionFun: (() -> Unit)? = null

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

        val settings = viewModel.getSettings()

        alertsViewModel.loadingLiveData.observe(viewLifecycleOwner) {
            if (it)
                showLoading("adding your alert")
            else
                hideLoading()

        }

        mainActivity.activityResultLiveData.observe(viewLifecycleOwner) {
            requestOverLayPermission(afterPermissionFun);
        }


        setupRecycleView()

        ItemTouchHelper(ViewHelpers.SwipeToRemove { position ->
            val alert = alertAdapter.differ.currentList[position]

            //delete article
            alertsViewModel.deleteAlert(alert)

            // set undo option
            showSnackbar2(getString(R.string.remove_alert_done_successfully)) {
                lifecycleScope.launch {
                    alertsViewModel.addAlert(alert)
                }
            }
        })
            .attachToRecyclerView(binding.rvAlerts)

        // observe to live data
        alertsViewModel.getData().observe(viewLifecycleOwner) { alerts ->
            when(alerts.isEmpty()){
                true->binding.linearHaventAny.visibility = View.VISIBLE
                false->binding.linearHaventAny.visibility = View.GONE
            }
            alertAdapter.differ.submitList(alerts)
        }

        binding.floatingActionButton.setOnClickListener {
            if (settings.location == null)
                showSnackbarWithoutAction(requireContext().getString(R.string.looks_like_you_haven_t_set_a_location_yet))
            else
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

    fun showSnackbar2(message: String? = null, undoAction: View.OnClickListener) {
        showSnackbar(message,undoAction,binding.floatingActionButton)
//        Snackbar.make(binding.root, message ?: "", Snackbar.LENGTH_LONG).apply {
//            setAction("UNDO", undoAction)
//            show()
//        }
    }

    fun showSnackbarWithoutAction(message: String? = null) {
        showSnackbar(message, anchorView = binding.floatingActionButton)
//        Snackbar.make(binding.root, message ?: "", Snackbar.LENGTH_LONG).apply {
//            show()
//        }
    }

    fun requestOverLayPermission(afterPermission: (() -> Unit)?) {
        @Suppress("DEPRECATION")
        if (!Settings.canDrawOverlays(context)) {
            afterPermissionFun = afterPermission

            val intent =
                Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${context?.packageName}")
                )
            startActivityForResult(intent, 500)
        } else {
            (afterPermission ?: afterPermissionFun)?.invoke()
        }
    }


    fun getDateTime() {

    }



}