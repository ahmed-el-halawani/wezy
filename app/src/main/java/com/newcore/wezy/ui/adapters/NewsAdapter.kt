package com.newcore.wezy.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.newcore.wezy.databinding.ItemArticlePreviewBinding
import com.newcore.wezy.models.Article


class NewsAdapter : RecyclerView.Adapter<NewsAdapter.ViewHate>() {

    data class ViewHate(val binding: ItemArticlePreviewBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHate {
        return ViewHate(
            ItemArticlePreviewBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int =
        differ.currentList.size

    override fun onBindViewHolder(holder: ViewHate, position: Int) {
        val article = differ.currentList[position]
        holder.binding.apply {
            Glide.with(root)
                .load(article.urlToImage)
                .into(ivArticleImage)

            tvSource.text = article.source?.name ?: ""
            tvTitle.text = article.title
            tvDescription.text = article.description
            tvPublishedAt.text = article.publishedAt

            root.setOnClickListener {
                onItemClickListener?.let { it(article) }
            }
        }
    }

    // set listeners
    fun setOnItemClickListener(onItemClickListener: ((Article) -> Unit)?) {
        this.onItemClickListener = onItemClickListener
    }

    // using DiffUtil to update our recycle
    // when update or change list of items
    private val differCallback = object : DiffUtil.ItemCallback<Article>() {
        override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean =
            oldItem.url == newItem.url

        override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean =
            oldItem == newItem
    }

    val differ = AsyncListDiffer(this, differCallback)


    // private vars
    private var onItemClickListener: ((Article) -> Unit)? = null

}