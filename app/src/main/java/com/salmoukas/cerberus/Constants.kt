package com.salmoukas.cerberus

import android.app.AlarmManager

class Constants {
    companion object {
        // keep alive
        const val KEEP_ALIVE_ACTION = "com.salmoukas.cerberus.KEEP_ALIVE"
        const val KEEP_ALIVE_INTERVAL_MILLISECONDS = AlarmManager.INTERVAL_HOUR

        // monitoring notification
        const val MONITORING_NOTIFICATION_CHANNEL = "monitoring_state"
        const val MONITORING_NOTIFICATION_ID = 1

        // check cycle
        const val CHECK_CYCLE_ACTION = "com.salmoukas.cerberus.CHECK_CYCLE"
        const val CHECK_CYCLE_INTERVAL_MINUTES = 1
        const val CHECK_CYCLE_REFERENCE_SUCCESS_RATIO: Double = 2 / 3.toDouble()

        // check sample
        const val CHECK_SAMPLE_MAX_VALIDITY_MINUTES = CHECK_CYCLE_INTERVAL_MINUTES + 3

        // check status
        const val CHECK_STATUS_REFRESH_INTERVAL_MILLISECONDS: Long = 1000 * 60
        const val CHECK_STATUS_WINDOW_SECONDS: Long = 60 * 60 * 24
    }
}
