package com.newcore.wezy.utils

import android.app.Activity
import android.content.res.Resources
import android.os.Build
import android.view.View
import android.widget.AbsListView
import android.widget.EditText
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.newcore.wezy.R
import java.util.*

object ViewHelpers {

    class SwipeToRemove(
        private val swipe:(position:Int)->Unit,
    ) : ItemTouchHelper.SimpleCallback(
    ItemTouchHelper.UP or ItemTouchHelper.DOWN,
    ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
    ){
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean = true

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.adapterPosition

            swipe(position)


        }

    }

     fun setAppLocale(localeCode: String,resources: Resources,reBuildActivity: Activity) {

        val resources = resources
        val dm = resources.displayMetrics
        val config = resources.configuration
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLocale(Locale(localeCode.lowercase(Locale.getDefault())))
        } else {
            config.locale = Locale(localeCode.lowercase(Locale.getDefault()))
        }
        resources.updateConfiguration(config, dm)

        ActivityCompat.recreate(reBuildActivity)
    }


    fun undoSnackbar(view:View,message:String?=null,undoAction:View.OnClickListener){
        Snackbar.make(view,message?:"article deleted successfully", Snackbar.LENGTH_LONG).apply {
            setAction("UNDO",undoAction)
            show()
        }
    }
}