package com.salmoukas.cerberus

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import java.util.*

class MainReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("MAIN_RECEIVER", "action received = " + intent?.action)
        if (context != null) {
            // perform check cycle if requested
            if (intent?.action == Constants.INTENT_CHECK_CYCLE) {
                MainService.ctlRunCheckCycle(context)
            }
            // start main service (if not started)
            else {
                MainService.ctlStartService(context)
            }
        }
    }

    companion object {
        fun ctlInstallKeepAlive(context: Context) {
            Log.d("MAIN_RECEIVER", "install keep alive")
            (context.applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager).setInexactRepeating(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + Constants.INTERVAL_KEEP_ALIVE,
                Constants.INTERVAL_KEEP_ALIVE,
                PendingIntent.getBroadcast(
                    context.applicationContext,
                    0,
                    Intent(
                        context.applicationContext,
                        MainReceiver::class.java
                    ).setAction(Constants.INTENT_KEEP_ALIVE),
                    0
                )
            )
        }

        fun ctlScheduleCheckCycle(context: Context) {
            // calculate delay
            val delay =
                Calendar.getInstance().apply {
                    timeInMillis = System.currentTimeMillis()
                    set(
                        Calendar.MINUTE,
                        get(Calendar.MINUTE) - (get(Calendar.MINUTE) % Constants.INTERVAL_CHECK_CYCLE_MINUTES) + Constants.INTERVAL_CHECK_CYCLE_MINUTES
                    )
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                    .timeInMillis - System.currentTimeMillis()


            // schedule next job execution
            Log.d(
                "MAIN_RECEIVER",
                "re-schedule check cycle with timeout of " + delay.toString() + "ms"
            )
            (context.applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager).setExactAndAllowWhileIdle(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + delay,
                PendingIntent.getBroadcast(
                    context.applicationContext,
                    0,
                    Intent(
                        context.applicationContext,
                        MainReceiver::class.java
                    ).setAction(Constants.INTENT_CHECK_CYCLE),
                    0
                )
            )
        }

        fun ctlCancelCheckCycle(context: Context) {
            Log.d("MAIN_RECEIVER", "cancel pending check cycle")
            (context.applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager).cancel(
                PendingIntent.getBroadcast(
                    context.applicationContext,
                    0,
                    Intent(
                        context.applicationContext,
                        MainReceiver::class.java
                    ).setAction(Constants.INTENT_CHECK_CYCLE),
                    0
                )
            )
        }
    }
}
