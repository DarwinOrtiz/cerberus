package com.salmoukas.cerberus

import android.app.AlarmManager

class Constants {
    companion object {
        // keep alive
        const val INTENT_KEEP_ALIVE = "com.salmoukas.cerberus.KEEP_ALIVE"
        const val INTERVAL_KEEP_ALIVE = AlarmManager.INTERVAL_HOUR
        const val WAKE_LOCK_KEEP_ALIVE_RECEIVER = "com.salmoukas.cerberus:KeepAliveReceiver"

        // notification
        const val NOTIFICATION_CHANNEL_MONITORING_STATE = "monitoring_state"
        const val NOTIFICATION_ID_MONITORING_STATE = 1

        // check cycle
        const val INTERVAL_CHECK_CYCLE_MINUTES = 1 // 5
        const val JOB_ID_CHECK_CYCLE = 1
    }
}
