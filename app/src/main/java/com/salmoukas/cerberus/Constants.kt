package com.salmoukas.cerberus

import android.app.AlarmManager

class Constants {
    companion object {
        // keep alive
        const val KEEP_ALIVE_ACTION = "com.salmoukas.cerberus.KEEP_ALIVE"
        const val KEEP_ALIVE_INTERVAL_MILLISECONDS = AlarmManager.INTERVAL_HOUR

        // monitoring notifications
        const val MONITORING_PRIMARY_NOTIFICATION_CHANNEL = "monitoring_primary"
        const val MONITORING_PRIMARY_NOTIFICATION_ID = 1
        const val MONITORING_ERROR_NOTIFICATION_CHANNEL = "monitoring_error"
        const val MONITORING_ERROR_NOTIFICATION_ID = 2
        const val MONITORING_ERROR_NOTIFICATION_NOREPEAT_MILLISECONDS = AlarmManager.INTERVAL_HALF_HOUR

        // check cycle
        const val CHECK_CYCLE_ACTION = "com.salmoukas.cerberus.CHECK_CYCLE"
        const val CHECK_CYCLE_INTERVAL_MINUTES = 10
        const val CHECK_CYCLE_REFERENCE_SUCCESS_RATIO: Double = 2 / 3.toDouble()

        // check status
        const val CHECK_STATUS_RETROGRADE_VALIDITY_MINUTES = CHECK_CYCLE_INTERVAL_MINUTES + 5
        const val CHECK_STATUS_REFRESH_INTERVAL_MILLISECONDS: Long = 1000 * 60
        const val CHECK_STATUS_PERIOD_SECONDS: Long = 60 * 60 * 24
        const val CHECK_STATUS_STALE_AFTER_SECONDS: Long = CHECK_CYCLE_INTERVAL_MINUTES * 3 * 60L
    }
}
