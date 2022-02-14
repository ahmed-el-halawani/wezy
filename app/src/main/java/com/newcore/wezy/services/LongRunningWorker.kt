package com.newcore.wezy.services

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.google.android.gms.maps.model.LatLng
import com.newcore.wezy.R
import com.newcore.wezy.WeatherApplication
import com.newcore.wezy.localDb.WeatherDatabase
import com.newcore.wezy.models.weatherentities.WeatherLang
import com.newcore.wezy.repository.RepoErrors
import com.newcore.wezy.repository.WeatherRepo
import com.newcore.wezy.shareprefrances.SettingsPreferences
import com.newcore.wezy.ui.notification.NotificationActivity
import com.newcore.wezy.utils.Constants.ALERT_BODY
import com.newcore.wezy.utils.Constants.ALERT_TITLE
import com.newcore.wezy.utils.Constants.CHANNEL_ID
import com.newcore.wezy.utils.Constants.EXTRA_NOTIFICATION_ID_CUSTOM
import com.newcore.wezy.utils.Constants.MY_ALERT_ID
import com.newcore.wezy.utils.Constants.MY_ALERT_LAT
import com.newcore.wezy.utils.Constants.MY_ALERT_LNG
import com.newcore.wezy.utils.Constants.MY_ALERT_TO
import com.newcore.wezy.utils.Either
import com.newcore.wezy.utils.ViewHelpers
import kotlinx.coroutines.delay
import java.util.*


class LongRunningWorker(context: Context, parameters: WorkerParameters) :
    CoroutineWorker(context, parameters) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as
                NotificationManager

    override suspend fun doWork(): Result {

        val notificationId = inputData.getInt(MY_ALERT_ID,0)
        val lat = inputData.getDouble(MY_ALERT_LAT,0.0)
        val lng = inputData.getDouble(MY_ALERT_LNG,0.0)
        val to = inputData.getLong(MY_ALERT_TO,0)

        val repo = WeatherRepo(
            SettingsPreferences(applicationContext as WeatherApplication),
            WeatherDatabase(applicationContext)
        )

        val settings = repo.getSettings()

        val res: Either<WeatherLang, RepoErrors> =  repo.getAlert(applicationContext, LatLng(lat,lng))

        val happyTitle = applicationContext.getString(R.string.happy_title)
        val happyBody = applicationContext.getString(R.string.happy_body)

        Log.e("res from worker", "doWork: $res", )

        val title = when(res){
            is Either.Error -> happyTitle
            is Either.Success -> {
                val weather = ViewHelpers.returnByLanguage(
                    settings.language,
                    res.data.arabicResponse,
                    res.data.englishResponse)
                    weather?.alerts?.run {
                        if(isNotEmpty()){
                            get(0).event
                        }else{
                            happyTitle
                        }
                    }?:happyTitle

            }
        }

        val body = when(res){
            is Either.Error -> happyBody
            is Either.Success -> {
                val weather = ViewHelpers.returnByLanguage(
                    settings.language,
                    res.data.arabicResponse,
                    res.data.englishResponse)
                    weather?.alerts?.run {
                        if(isNotEmpty()){
                            get(0).description
                        }else{
                            happyBody
                        }
                    }?:happyBody
            }
        }

        val openNotificationDialog = Intent(applicationContext, NotificationActivity::class.java)
            .apply {
                putExtra(EXTRA_NOTIFICATION_ID_CUSTOM,notificationId)
                putExtra(ALERT_TITLE,title)
                putExtra(ALERT_BODY,body)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

        pushNotification(openNotificationDialog,notificationId,title,body)

        applicationContext.startActivity(openNotificationDialog)

        delay(to - Date().time)
        applicationContext.sendBroadcast(
            Intent(applicationContext, StopAlarmBroadcast::class.java)
                .putExtra(EXTRA_NOTIFICATION_ID_CUSTOM,notificationId)
        )

        return Result.success()
    }


//
//    suspend private fun createNotification3(): Notification {
//        val id = CHANNEL_ID
//        val title = "HI"
//        val cancel = "CANCEL"
//
//        val i = PendingIntent.getActivity(applicationContext, 0, Intent(), 0);
//
//        val customView = RemoteViews(applicationContext.packageName,R.layout.notification_banner)
//
//
////        customView.setOnClickPendingIntent(R.id.btnDissmiss)
//
//        val n =  NotificationCompat.Builder(applicationContext, id)
//            .setContentText("progress")
//            .setSmallIcon(R.drawable.defalut_icon)
//            .setPriority(NotificationCompat.PRIORITY_HIGH)
//            .setFullScreenIntent(i,true)
//            .setCategory(Notification.CATEGORY_CALL)
//            .setCustomBigContentView(customView)
//            .setCustomContentView(customView)
//
//            .setOngoing(true)
//            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
//            // Add the cancel action to the notification which can
//            // be used to cancel the worker
////            .addAction(android.R.drawable.ic_delete, cancel, intent)
//            .build()
//
//
////        setForeground(ForegroundInfo(1,n))
//        return n;
//    }


    @SuppressLint("LaunchActivityFromNotification")
    fun pushNotification(openNotificationDialog: Intent, notificationId:Int, title:String, body:String) {

        val snoozeIntent = Intent(applicationContext, StopAlarmBroadcast::class.java)
            .putExtra(EXTRA_NOTIFICATION_ID_CUSTOM, notificationId)

        val snoozePendingIntent = PendingIntent
            .getBroadcast(applicationContext,
                notificationId,
                snoozeIntent,
                Intent.FILL_IN_DATA
            )

        val openAlert: PendingIntent? = TaskStackBuilder.create(applicationContext).run {
            addNextIntentWithParentStack(openNotificationDialog)
            getPendingIntent(0, 0)
        }

        val builder: NotificationCompat.Builder = NotificationCompat.Builder(applicationContext,CHANNEL_ID )
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle())
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(openAlert)
            .addAction(R.drawable.defalut_icon,"Remove",snoozePendingIntent)

        NotificationManagerCompat.from(applicationContext).notify(notificationId,builder.build())

    }
//
//    private fun createNotification() {
//        val id = CHANNEL_ID
//        val title = "WEZY Alert"
//        val notificationId = Random().nextInt()
//
//        val remote = RemoteViews(applicationContext.packageName,R.layout.notification_banner)
//
//
//        val openAlert: PendingIntent? = TaskStackBuilder.create(applicationContext).run {
//            addNextIntentWithParentStack(Intent())
//            getPendingIntent(0, 0)
//        }
//
//        val snoozeIntent = Intent(applicationContext, StopAlarmBroadcast::class.java)
//            .putExtra(EXTRA_NOTIFICATION_ID_CUSTOM, notificationId)
//        val snoozePendingIntent = PendingIntent.getBroadcast(applicationContext,
//            notificationId, snoozeIntent, Intent.FILL_IN_DATA)
//
//        remote.setOnClickPendingIntent(R.id.btnDissmiss,snoozePendingIntent)
//
//        val n =  NotificationCompat.Builder(applicationContext, id)
//            .setSmallIcon(R.drawable.defalut_icon)
//            .setPriority(NotificationCompat.PRIORITY_HIGH)
//            .setOngoing(true)
//            .setFullScreenIntent(openAlert,true)
//            .setCustomContentView(remote)
//            .setCustomBigContentView(remote)
//            .build()
//
//        NotificationManagerCompat.from(applicationContext).notify(notificationId,n)
//
//    }

    private fun createForegroundInfo(progress: String): ForegroundInfo {
        val id = CHANNEL_ID
        val title = "HI"
        val cancel = "CANCEL"
        // This PendingIntent can be used to cancel the worker
        val intent = WorkManager.getInstance(applicationContext)
            .createCancelPendingIntent(getId())


        // Create a Notification channel if necessary
        createChannel()

        val notification = NotificationCompat.Builder(applicationContext, id)
            .setContentTitle(title)
            .setTicker(title)
            .setContentText(progress)
            .setSmallIcon(R.drawable.defalut_icon)
            .setOngoing(true)

            // Add the cancel action to the notification which can
            // be used to cancel the worker
            .addAction(android.R.drawable.ic_delete, cancel, intent)
            .build()

        return ForegroundInfo(1,notification)
    }

    private fun createChannel() {
        // Create the NotificationChannel
        val name = applicationContext.getString(R.string.channel_name)
        val descriptionText ="setting weather alarm"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val mChannel = NotificationChannel(CHANNEL_ID, name, importance)
        mChannel.description = descriptionText
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        notificationManager.createNotificationChannel(mChannel)
    }

    companion object {
        const val KEY_INPUT_URL = "KEY_INPUT_URL"
        const val KEY_OUTPUT_FILE_NAME = "KEY_OUTPUT_FILE_NAME"
    }
}