package com.newcore.wezy.ui.homescreen

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.newcore.wezy.WeatherApplication
import com.newcore.wezy.databinding.ItemNext7DaysBinding
import com.newcore.wezy.models.Article
import com.newcore.wezy.models.weatherentities.Daily
import com.newcore.wezy.shareprefrances.SettingsPreferences
import com.newcore.wezy.utils.ViewHelpers
import com.newcore.wezy.utils.ViewHelpers.convertFromKelvin
import com.newcore.wezy.utils.ViewHelpers.getDateFromUnix
import com.newcore.wezy.utils.ViewHelpers.getDayFromUnix
import com.newcore.wezy.utils.ViewHelpers.numberLocalizer

class DailyAdapter : RecyclerView.Adapter<DailyAdapter.ViewHolder>() {
    data class ViewHolder(val binding: ItemNext7DaysBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            ItemNext7DaysBinding.inflate(
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
                tvDayViewer.text = getDayFromUnix(dt?.toLong(), settings.language)
                tvDate.text = getDateFromUnix(dt?.toLong(), settings.language)

                tvTemp.text =
                    convertFromKelvin(temp?.day, settings).numberLocalizer(settings.language)
                tvTempUnit.text = ViewHelpers.getStringTempUnit(settings.tempUnit)

                tvFeelsLikeTemp.text =
                    convertFromKelvin(feelsLike?.day, settings).numberLocalizer(settings.language)
                tvFeelsLikeTempUnit.text = ViewHelpers.getStringTempUnit(settings.tempUnit)

                Glide.with(root).load(weather[0].icon).into(ivWeatherIcon)
            }
        }
    }

    override fun getItemCount(): Int =
        differ.currentList.size


    // set listeners
    fun setOnItemClickListener(onItemClickListener: ((Article) -> Unit)?) {
        this.onItemClickListener = onItemClickListener
    }

    // using DiffUtil to update our recycle
    // when update or change list of items
    private val differCallback = object : DiffUtil.ItemCallback<Daily>() {
        override fun areItemsTheSame(oldItem: Daily, newItem: Daily): Boolean =
            oldItem.dt == newItem.dt

        override fun areContentsTheSame(oldItem: Daily, newItem: Daily): Boolean =
            oldItem == newItem
    }

    val differ = AsyncListDiffer(this, differCallback)


    // private vars
    private var onItemClickListener: ((Article) -> Unit)? = null
}