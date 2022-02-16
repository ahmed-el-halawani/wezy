package com.newcore.wezy.ui.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.newcore.wezy.R
import com.newcore.wezy.databinding.ActivityNotificationBinding
import com.newcore.wezy.services.StopAlarmBroadcast
import com.newcore.wezy.utils.Constants.ALERT_BODY
import com.newcore.wezy.utils.Constants.ALERT_COUNTRY
import com.newcore.wezy.utils.Constants.ALERT_TITLE
import com.newcore.wezy.utils.Constants.EXTRA_NOTIFICATION_ID_CUSTOM
import com.tapadoo.alerter.Alerter


class NotificationActivity : AppCompatActivity() {
    val binding: ActivityNotificationBinding by lazy {
        ActivityNotificationBinding.inflate(layoutInflater)
    }

    val notificationId by lazy {
        intent.getIntExtra(EXTRA_NOTIFICATION_ID_CUSTOM,0)
    }

    val title by lazy {
        intent.getStringExtra(ALERT_TITLE)
    }
    val body by lazy {
        intent.getStringExtra(ALERT_BODY)
    }

    val country by lazy {
        intent.getStringExtra(ALERT_COUNTRY)
    }

    val media by lazy{
        MediaPlayer.create(this, R.raw.alert)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        media.isLooping = true
        media.start()



        window.statusBarColor = getColor(R.color.transparent)
        window.navigationBarColor = getColor(R.color.transparent)

        registerReceiver(mReceiver, IntentFilter("finish_activity"));

        binding.root.setOnClickListener {
            finishIt()
        }

        val alerter = Alerter.create(this)
            .setTitle(title?:"NoData")
            .setText(body?:"NoData")
            .enableInfiniteDuration(true)
            .setBackgroundColorRes(R.color.surfaceColor)
            .addButton("Dismiss", com.tapadoo.alerter.R.style.AlertButton) {
                finishIt()
            }
            .addButton("Cancel", com.tapadoo.alerter.R.style.AlertButton) {
                cancelIt()
            }
            .setOnClickListener {}
            .enableSwipeToDismiss()

        alerter.show()
    }


    private fun cancelIt() {

        sendBroadcast(
            Intent(applicationContext, StopAlarmBroadcast::class.java)
                .putExtra(EXTRA_NOTIFICATION_ID_CUSTOM,notificationId)
        )

    }

    private fun finishIt() {

        finishAndRemoveTask();
        this@NotificationActivity.overridePendingTransition(androidx.constraintlayout.widget.R.anim.abc_fade_in,
            com.tapadoo.alerter.R.anim.alerter_slide_out_to_top);
    }


    var mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            finishAndRemoveTask();
            this@NotificationActivity.overridePendingTransition(androidx.constraintlayout.widget.R.anim.abc_fade_in,
                com.tapadoo.alerter.R.anim.alerter_slide_out_to_top);
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        media.stop()
    }

}