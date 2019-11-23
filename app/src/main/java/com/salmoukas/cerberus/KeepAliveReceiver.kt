package com.salmoukas.cerberus

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager

class KeepAliveReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {

        // ensure service can start properly
        if (context != null) {
            (context.getSystemService(Context.POWER_SERVICE) as PowerManager)
                .newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK,
                    Constants.WAKE_LOCK_KEEP_ALIVE_RECEIVER
                )
                .acquire(10 * 1000)
        }

        // start main service (if not started)
        context?.startForegroundService(Intent(context, MainService::class.java))
    }
}
