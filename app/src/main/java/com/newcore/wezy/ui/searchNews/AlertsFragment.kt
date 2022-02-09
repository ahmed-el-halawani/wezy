package com.newcore.wezy.ui.searchNews

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.newcore.wezy.databinding.FragmentSearchNewsBinding
import com.newcore.wezy.ui.BaseFragment
import com.newcore.wezy.ui.adapters.NewsAdapter
import com.newcore.wezy.utils.*

class AlertsFragment
    :BaseFragment<FragmentSearchNewsBinding>( FragmentSearchNewsBinding::inflate ) {


    private val newsAdapter by lazy{
        NewsAdapter().apply {
            setOnItemClickListener {}
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecycleView()

//
//        newsViewModel.searchNewsLiveData.observe(viewLifecycleOwner) { resource ->
//            when (resource){
//                is Resource.Loading ->showLoading()
//                is Resource.Success -> {
//                    hideLoading()
//                }
//                is Resource.Error -> {
//                    hideLoading()
//
//                    when (resource.message){
//                        Constants.No_INTERNET_CONNECTION -> networkState.showNoInternet()
//                        else -> Log.e(Constants.BREAKING_ERROR_TAG, resource.message?:"" )
//                    }
//
//                }
//            }
//        }



    }

    private fun setupRecycleView(){
        binding.rvSearchNews.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
        }
    }

}