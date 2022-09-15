package com.newcore.wezy.services

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
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
import com.demo.data.shareprefrances.SettingsPreferences
import com.newcore.wezy.ui.notification.NotificationActivity
import com.demo.core.utils.Constants.ALERT_BODY
import com.demo.core.utils.Constants.ALERT_COUNTRY
import com.demo.core.utils.Constants.ALERT_TITLE
import com.demo.core.utils.Constants.CHANNEL_ID
import com.demo.core.utils.Constants.EXTRA_NOTIFICATION_ID_CUSTOM
import com.demo.core.utils.Constants.IS_ALERT
import com.demo.core.utils.Constants.MY_ALERT_ID
import com.demo.core.utils.Constants.MY_ALERT_LAT
import com.demo.core.utils.Constants.MY_ALERT_LNG
import com.demo.core.utils.Constants.MY_ALERT_TO
import com.demo.core.utils.Either
import com.newcore.wezy.ui.utils.ViewHelpers
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
        val isAlert = inputData.getBoolean(IS_ALERT,true)

        val repo = WeatherRepo(
            SettingsPreferences(applicationContext as WeatherApplication),
            WeatherDatabase(applicationContext)
        )

        val settings = repo.getSettings()

        val res: Either<WeatherLang, RepoErrors> =  repo.getAlert(applicationContext, LatLng(lat,lng))

        val happyTitle = ViewHelpers.returnByLanguage(
            settings.language,
            "WEZY لديها أخبار سارة لك \uD83D\uDE0A",
            "WEZY has good news for you \uD83D\uDE0A"
        )

        val happyBody = ViewHelpers.returnByLanguage(
            settings.language,
            "لا توجد تنبيهات سيئة لهذه المدة ،\n" +
                    "أتمنى أن تستمتع ببقية يومك",
            "there is no bad alerts for this duration,\n" +
                    "hope you enjoy the rest of your day"
        )

        var title = happyTitle

        var body = happyBody

        var country = ""

        when(res){
            is Either.Error -> {}
            is Either.Success -> {
                val weather = ViewHelpers.returnByLanguage(
                    settings.language,
                    res.data.arabicResponse,
                    res.data.englishResponse
                )

                country = weather?.country?:""

                weather?.alerts?.run {
                    forEach {
                        val start = ViewHelpers.getDateObjectFromUnix(it.start?.toLong())
                        val end = ViewHelpers.getDateObjectFromUnix(it.end?.toLong())
                        if((start!=null && start<=Date(to))||
                            (end!=null && end<=Date(to))
                        ){
                            title = it.event?:""
                            body = ViewHelpers.returnByLanguage(
                                settings.language,
                                "عنوان :"+"$country\n"+"انذار :"+" ${it.description?:""}",
                                "Address: $country\nAlert: ${it.description?:""}"
                            )
                        }
                    }
                }
            }
        }

if(isAlert){
    val openNotificationDialog = Intent(applicationContext, NotificationActivity::class.java)
        .apply {
            putExtra(EXTRA_NOTIFICATION_ID_CUSTOM,notificationId)
            putExtra(ALERT_TITLE,title)
            putExtra(ALERT_BODY,body)
            putExtra(ALERT_COUNTRY,country)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        }

    pushNotification(openNotificationDialog,notificationId,title,body,country)

    applicationContext.startActivity(openNotificationDialog)
}else{
    pushNotification(null,notificationId,title,body,country)
}


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
    fun pushNotification(openNotificationDialog: Intent?, notificationId:Int, title:String, body:String, country:String) {

        val snoozeIntent = Intent(applicationContext, StopAlarmBroadcast::class.java)
            .putExtra(EXTRA_NOTIFICATION_ID_CUSTOM, notificationId)

        val snoozePendingIntent = PendingIntent
            .getBroadcast(applicationContext,
                notificationId,
                snoozeIntent,
                Intent.FILL_IN_DATA
            )

        val openAlert: PendingIntent? = TaskStackBuilder.create(applicationContext).run {
            addNextIntentWithParentStack(openNotificationDialog?:Intent())
            getPendingIntent(notificationId, Intent.FILL_IN_DATA)
        }

        val builder: NotificationCompat.Builder = NotificationCompat.Builder(applicationContext,CHANNEL_ID )
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setContentTitle(title)
            .setContentText(
                body
            )
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