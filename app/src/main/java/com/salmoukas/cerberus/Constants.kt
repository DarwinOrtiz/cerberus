package com.salmoukas.cerberus

import android.app.AlarmManager

class Constants {
    companion object {
        // keep alive
        const val INTENT_KEEP_ALIVE = "com.salmoukas.cerberus.KEEP_ALIVE"
        const val INTERVAL_KEEP_ALIVE = AlarmManager.INTERVAL_HOUR
        const val WAKE_LOCK_MAIN_SERVICE = "com.salmoukas.cerberus:MainService"

        // notification
        const val NOTIFICATION_CHANNEL_MONITORING_STATE = "monitoring_state"
        const val NOTIFICATION_ID_MONITORING_STATE = 1

        // check cycle
        const val INTERVAL_CHECK_CYCLE_MINUTES = 1 // 5
        const val CHECK_CYCLE_REQUIRED_REFERENCE_SUCCESS = 2 / 3.toDouble()

        const val INTENT_CHECK_CYCLE = "com.salmoukas.cerberus.CHECK_CYCLE"
    }
}
