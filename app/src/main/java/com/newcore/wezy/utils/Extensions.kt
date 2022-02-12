package com.newcore.wezy.utils

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.core.view.forEach
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.ui.NavigationUI
import com.google.android.material.navigation.NavigationBarView
import java.lang.ref.WeakReference

object Extensions {
    fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
        observe(lifecycleOwner, object : Observer<T> {
            override fun onChanged(t: T?) {
                observer.onChanged(t)
                removeObserver(this)
            }
        })
    }



    public fun NavigationBarView.setupWithNavController2(
        navController: NavController,
        menuAndEquals: Map<Int, List<Int>>
    ) {
        setOnItemSelectedListener { item ->
            NavigationUI.onNavDestinationSelected(
                item,
                navController
            )
        }
        val weakReference = WeakReference(this)
        navController.addOnDestinationChangedListener(
            object : NavController.OnDestinationChangedListener {
                override fun onDestinationChanged(
                    controller: NavController,
                    destination: NavDestination,
                    arguments: Bundle?
                ) {
                    val view = weakReference.get()
                    if (view == null) {
                        navController.removeOnDestinationChangedListener(this)
                        return
                    }
                    view.menu.forEach { item ->
                        if(menuAndEquals[item.itemId]!=null){
                            menuAndEquals[item.itemId]?.forEach {
                                if (destination.matchDestination(it)) {
                                    item.isChecked = true
                                }
                            }
                        }else{
                            if (destination.matchDestination(item.itemId)) {
                                item.isChecked = true
                            }
                        }
                    }
                }
            })
    }

    internal fun NavDestination.matchDestination(@IdRes destId: Int): Boolean =
        hierarchy.any { it.id == destId }


    fun <T>List<T>.where(p:(T)->Boolean): ArrayList<T> {
        val l:ArrayList<T> = ArrayList()
        forEach {
            if(p(it)) l.add(it)
        }
        return l
    }

}