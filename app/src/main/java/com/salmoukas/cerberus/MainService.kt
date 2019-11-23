package com.salmoukas.cerberus

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.IBinder

class MainService : Service() {

    override fun onCreate() {
        super.onCreate()

        // create notification channel
        NotificationChannel(
            Constants.NOTIFICATION_CHANNEL_MONITORING_STATE,
            resources.getString(R.string.app_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            enableVibration(false)
            enableLights(false)
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(this)
        }

        // create notification and post service to foreground
        Notification.Builder(this, Constants.NOTIFICATION_CHANNEL_MONITORING_STATE)
            .setContentTitle(resources.getString(R.string.app_name))
            .setContentText(resources.getString(R.string.monitoring_active))
            .setStyle(Notification.BigTextStyle().bigText(resources.getString(R.string.monitoring_active)))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(
                PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), 0)
            )
            .setOngoing(true)
            .build()
            .apply {
                startForeground(Constants.NOTIFICATION_ID_MONITORING_STATE, this)
            }

        // schedule check cycle job
        CheckCycleJob.scheduleJob(this)
    }

    override fun onDestroy() {
        super.onDestroy()

        // cancel check cycle job
        CheckCycleJob.cancelJob(this)

        // remove from foreground
        stopForeground(true)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
