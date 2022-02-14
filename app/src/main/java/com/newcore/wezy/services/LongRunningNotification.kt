package com.newcore.wezy.services

import android.app.Notification
import android.content.Context
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.newcore.wezy.R
import com.newcore.wezy.utils.Constants.CHANNEL_ID


class LongRunningNotification(val appContext: Context, workerParams: WorkerParameters):
    CoroutineWorker(appContext, workerParams) {

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(
            5, createNotification()
        )
    }

    override suspend fun doWork(): Result {
        Toast.makeText(appContext, "hi from inside worker", Toast.LENGTH_SHORT).show()
        return Result.success()
    }

    private fun createNotification(): Notification {
        val id = CHANNEL_ID
        val title = "HI"
        val cancel = "CANCEL"

        return NotificationCompat.Builder(applicationContext, id)
            .setContentTitle(title)
            .setTicker(title)
            .setContentText("progress")
            .setSmallIcon(R.drawable.defalut_icon)
            .setOngoing(true)
            // Add the cancel action to the notification which can
            // be used to cancel the worker
//            .addAction(android.R.drawable.ic_delete, cancel, intent)
            .build()
    }

}