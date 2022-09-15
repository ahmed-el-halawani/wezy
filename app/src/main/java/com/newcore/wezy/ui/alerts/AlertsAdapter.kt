package com.newcore.wezy.ui.alerts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.newcore.wezy.WeatherApplication
import com.newcore.wezy.databinding.ItemAlertBinding
import com.newcore.wezy.models.MyAlert
import com.demo.data.shareprefrances.SettingsPreferences
import com.newcore.wezy.ui.utils.ViewHelpers
import com.newcore.wezy.ui.utils.ViewHelpers.returnByLanguage
import java.util.*

class AlertsAdapter : RecyclerView.Adapter<AlertsAdapter.ViewHolder>() {
    data class ViewHolder(val binding: ItemAlertBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            ItemAlertBinding.inflate(
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
                tvDtFrom.text = ViewHelpers.getTimeFromDate(Date(fromDT),settings.language)
                tvDtTo.text = ViewHelpers.getTimeFromDate(Date(toDT),settings.language)
                tvCountry.text = returnByLanguage(settings.language,arabicCountryName,englishCountryName)
            }
        }

    }

    override fun getItemCount(): Int =
        differ.currentList.size


    // set listeners
    fun setOnItemClickListener(onItemClickListener: ((MyAlert) -> Unit)?) {
        this.onItemClickListener = onItemClickListener
    }

    // using DiffUtil to update our recycle
    // when update or change list of items
    private val differCallback = object : DiffUtil.ItemCallback<MyAlert>() {
        override fun areItemsTheSame(oldItem: MyAlert, newItem: MyAlert): Boolean =
            oldItem.id == newItem.id


        override fun areContentsTheSame(oldItem: MyAlert, newItem: MyAlert): Boolean =
            oldItem == newItem
    }

    val differ = AsyncListDiffer(this, differCallback)


    // private vars
    private var onItemClickListener: ((MyAlert) -> Unit)? = null
}