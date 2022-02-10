package com.newcore.wezy.ui.savedNews

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.newcore.wezy.databinding.FragmentFavoriteBinding
import com.newcore.wezy.ui.BaseFragment
import com.newcore.wezy.ui.adapters.NewsAdapter

class FavoritesFragment
    : BaseFragment<FragmentFavoriteBinding>(FragmentFavoriteBinding::inflate) {

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