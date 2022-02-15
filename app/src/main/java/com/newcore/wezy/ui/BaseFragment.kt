package com.newcore.wezy.ui

import android.app.AlertDialog
import android.app.ProgressDialog
import android.app.ProgressDialog.show
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.accessibility.AccessibilityEventCompat.setAction
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.google.android.material.snackbar.Snackbar
import com.newcore.wezy.R
import com.newcore.wezy.utils.ILoading
import com.newcore.wezy.utils.INetwork

abstract class BaseFragment<T : ViewBinding>(val viewBindingInflater:(LayoutInflater)->T): Fragment(),
    ILoading {

    val  viewModel by lazy {
        (activity as MainActivity).appStateViewModel
    }

    val networkState by lazy{
        activity as INetwork
    }

    val binding by lazy {
        viewBindingInflater(layoutInflater)
    }

    val mainActivity by lazy{
        this.activity as MainActivity
    }

    fun getLocationWithGps() = mainActivity.getGpsLocation()

    fun setAppLocale(localeCode: String?=null)= mainActivity.setAppLocale(localeCode)

    open fun showSnackbar(message: String? = null, undoAction: View.OnClickListener?=null,anchorView: View?=null)= mainActivity.showSnackbar(message, undoAction,anchorView)

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            return binding.root
    }

    override fun showLoading(message:String?) {
        dialog = ProgressDialog.show(mainActivity, "",message?:getString(R.string.loading), true);
    }

    override fun hideLoading() {
        dialog?.dismiss()
    }

    var dialog: AlertDialog? = null


}