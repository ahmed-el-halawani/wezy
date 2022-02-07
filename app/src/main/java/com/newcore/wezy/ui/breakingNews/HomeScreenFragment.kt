package com.newcore.wezy.ui.breakingNews

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.newcore.wezy.R
import com.newcore.wezy.ui.adapters.NewsAdapter
import com.newcore.wezy.databinding.FragmentBreakingNewsBinding
import com.newcore.wezy.ui.BaseFragment
import com.newcore.wezy.utils.*
import com.newcore.wezy.utils.Constants.ARTICLE
import com.newcore.wezy.utils.Constants.BREAKING_ERROR_TAG
import com.newcore.wezy.utils.Constants.MAX_RESULT_FOR_FREE_API
import com.newcore.wezy.utils.Constants.No_INTERNET_CONNECTION
import com.newcore.wezy.utils.Constants.TOTAL_NUMBER_OF_ITEMS_PER_REQUEST

class HomeScreenFragment
    : BaseFragment<FragmentBreakingNewsBinding>( FragmentBreakingNewsBinding::inflate ) , ILoading {


    private val newsAdapter by lazy{
        NewsAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//
//        setupRecycleView()
//
//        newsViewModel.breakingNewsLiveData.observe(viewLifecycleOwner) {
//            when (it) {
//                is Resource.Loading -> showLoading()
//                is Resource.Success -> {
//
//                    hideLoading()
//                    networkState.hideNoInternet()
//
//                }
//                is Resource.Error -> {
//                    hideLoading()
//
//                    when (it.message){
//                        No_INTERNET_CONNECTION-> networkState.showNoInternet()
//                        else -> Log.e(BREAKING_ERROR_TAG, it.message?:"" )
//                    }
//
//                }
//            }
//        }
    }

    private fun setupRecycleView(){
        binding.rvBreakingNews.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
        }
    }

    override fun showLoading(){
        binding.paginationProgressBar.visibility = View.VISIBLE
    }

    override fun hideLoading(){
        binding.paginationProgressBar.visibility = View.INVISIBLE
    }


}