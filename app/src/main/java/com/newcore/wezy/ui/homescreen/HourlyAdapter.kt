package com.newcore.wezy.ui.homescreen

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.newcore.wezy.WeatherApplication
import com.newcore.wezy.databinding.ItemTimeTempBinding
import com.newcore.wezy.models.weatherentities.Hourly
import com.demo.data.shareprefrances.SettingsPreferences
import com.demo.core.utils.ApiViewHelper
import com.newcore.wezy.ui.utils.ViewHelpers
import com.newcore.wezy.ui.utils.ViewHelpers.convertFromKelvin
import com.newcore.wezy.ui.utils.ViewHelpers.numberLocalizer

class HourlyAdapter : RecyclerView.Adapter<HourlyAdapter.ViewHolder>() {
    data class ViewHolder(val binding: ItemTimeTempBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HourlyAdapter.ViewHolder =
        ViewHolder(
            ItemTimeTempBinding.inflate(
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
                tvHour.text = ViewHelpers.getHourFromUnix(dt?.toLong(), settings.language)

                tvTemp.text = convertFromKelvin(temp,settings)
                    .numberLocalizer(settings.language)


                weather.let {
                    if (it.isNotEmpty())
                        Glide.with(root).load(it[0].icon?.let { icon ->
                            ApiViewHelper.iconImagePathMaker(icon)
                        }).into(ivIcon)
                }

                tvTempUnit.text = ViewHelpers.getStringTempUnit(settings.tempUnit)
            }

        }
    }

    override fun getItemCount(): Int =
        differ.currentList.size


    // set listeners
    fun setOnItemClickListener(onItemClickListener: ((Hourly) -> Unit)?) {
        this.onItemClickListener = onItemClickListener
    }

    // using DiffUtil to update our recycle
    // when update or change list of items
    private val differCallback = object : DiffUtil.ItemCallback<Hourly>() {
        override fun areItemsTheSame(oldItem: Hourly, newItem: Hourly): Boolean =
            oldItem.dt == newItem.dt

        override fun areContentsTheSame(oldItem: Hourly, newItem: Hourly): Boolean =
            oldItem == newItem
    }

    val differ = AsyncListDiffer(this, differCallback)


    // private vars
    private var onItemClickListener: ((Hourly) -> Unit)? = null
}