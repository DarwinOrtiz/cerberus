package com.salmoukas.cerberus

import android.app.AlarmManager

class Constants {
    companion object {
        // keep alive
        const val KEEP_ALIVE_ACTION = "com.salmoukas.cerberus.KEEP_ALIVE"
        const val KEEP_ALIVE_INTERVAL = AlarmManager.INTERVAL_HOUR

        // monitoring notification
        const val MONITORING_NOTIFICATION_CHANNEL = "monitoring_state"
        const val MONITORING_NOTIFICATION_ID = 1

        // check cycle
        const val CHECK_CYCLE_ACTION = "com.salmoukas.cerberus.CHECK_CYCLE"
        const val CHECK_CYCLE_INTERVAL_MINUTES = 1
        const val CHECK_CYCLE_REFERENCE_SUCCESS_RATIO = 2 / 3.toDouble()

        // check sample
        const val CHECK_SAMPLE_MAX_VALIDITY_MINUTES = CHECK_CYCLE_INTERVAL_MINUTES + 3
    }
}
