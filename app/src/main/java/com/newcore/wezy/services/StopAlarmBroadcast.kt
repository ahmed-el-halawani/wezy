package com.newcore.wezy.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.work.WorkManager
import com.newcore.wezy.WeatherApplication
import com.newcore.wezy.localDb.WeatherDatabase
import com.newcore.wezy.repository.WeatherRepo
import com.demo.data.shareprefrances.SettingsPreferences
import com.demo.core.utils.Constants.EXTRA_NOTIFICATION_ID_CUSTOM
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StopAlarmBroadcast : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val notifyId: Int = intent.getIntExtra(EXTRA_NOTIFICATION_ID_CUSTOM, 0)

        Log.e("notifyId", "onReceive: $notifyId")

        val repo = WeatherRepo(
            SettingsPreferences(context.applicationContext as WeatherApplication),
            WeatherDatabase(context)
        )

        CoroutineScope(Dispatchers.IO).launch {
            repo.removeAlertWithId(notifyId.toLong())
        }

        WorkManager.getInstance(context).cancelUniqueWork(notifyId.toString())

        NotificationManagerCompat.from(context.applicationContext).cancel(notifyId)
        context.sendBroadcast(Intent("finish_activity"))

    }
}