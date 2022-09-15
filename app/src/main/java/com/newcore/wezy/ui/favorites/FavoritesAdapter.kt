package com.newcore.wezy.ui.favorites

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.newcore.wezy.WeatherApplication
import com.newcore.wezy.databinding.ItemFavoriteBinding
import com.newcore.wezy.models.weatherentities.WeatherLang
import com.demo.data.shareprefrances.SettingsPreferences
import com.newcore.wezy.ui.utils.ViewHelpers.getWeatherFromWeatherLang

class FavoritesAdapter : RecyclerView.Adapter<FavoritesAdapter.ViewHolder>() {
    data class ViewHolder(val binding: ItemFavoriteBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            ItemFavoriteBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val settings =
            SettingsPreferences(holder.itemView.context.applicationContext as WeatherApplication).get()

        val item = differ.currentList[position]
        holder.binding.apply {
            item.apply {
                val weather = getWeatherFromWeatherLang(settings, this)
                tvCountry.text = weather?.country
                tvAddressLine.text = weather?.addressLine
                holder.binding.root.setOnClickListener {
                    onItemClickListener?.invoke(this)
                }
            }
        }
    }

    override fun getItemCount(): Int =
        differ.currentList.size


    // set listeners
    fun setOnItemClickListener(onItemClickListener: ((WeatherLang) -> Unit)?) {
        this.onItemClickListener = onItemClickListener
    }

    // using DiffUtil to update our recycle
    // when update or change list of items
    private val differCallback = object : DiffUtil.ItemCallback<WeatherLang>() {
        override fun areItemsTheSame(oldItem: WeatherLang, newItem: WeatherLang): Boolean =
            oldItem.lat == newItem.lat &&
            oldItem.lon == newItem.lon


        override fun areContentsTheSame(oldItem: WeatherLang, newItem: WeatherLang): Boolean =
            oldItem == newItem
    }

    val differ = AsyncListDiffer(this, differCallback)


    // private vars
    private var onItemClickListener: ((WeatherLang) -> Unit)? = null
}