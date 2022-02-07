package com.newcore.wezy.ui.savedNews

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.newcore.wezy.R
import com.newcore.wezy.ui.BaseFragment
import com.newcore.wezy.databinding.FragmentSavedNewsBinding
import com.newcore.wezy.ui.adapters.NewsAdapter
import com.newcore.wezy.utils.Constants.ARTICLE
import com.newcore.wezy.utils.ViewHelpers

class FavoritesFragment
    : BaseFragment<FragmentSavedNewsBinding>(FragmentSavedNewsBinding::inflate) {

    private val newsAdapter by lazy {
        NewsAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//
//        setupRecycleView()
//
//        ItemTouchHelper(ViewHelpers.SwipeToRemove { position ->
//            val article = newsAdapter.differ.currentList[position]
//
//            //delete article
//            newsViewModel.deleteArticle(article)
//
//            // set undo option
//            ViewHelpers.undoSnackbar(view) {
//                newsViewModel.saveArticle(article)
//            }
//        })
//            .attachToRecyclerView(binding.rvSavedNews)
//
//        // observe to live data
//        newsViewModel.getSavedNews().observe(viewLifecycleOwner) {
//            newsAdapter.differ.submitList(it)
//        }
    }

    private fun setupRecycleView() {
        binding.rvSavedNews.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
        }
    }
}