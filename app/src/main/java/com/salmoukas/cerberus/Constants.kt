package com.salmoukas.cerberus

class Constants {
    companion object {
        // keep alive
        const val KEEP_ALIVE_ACTION = "com.salmoukas.cerberus.KEEP_ALIVE"
        const val KEEP_ALIVE_INTERVAL_MINUTES = 60

        // monitoring notifications
        const val MONITORING_PRIMARY_NOTIFICATION_CHANNEL = "monitoring_primary"
        const val MONITORING_PRIMARY_NOTIFICATION_ID = 1
        const val MONITORING_ERROR_NOTIFICATION_CHANNEL = "monitoring_error"
        const val MONITORING_ERROR_NOTIFICATION_ID = 2
        const val MONITORING_ERROR_NOTIFICATION_NOREPEAT_MINUTES = 30

        // check cycle
        const val CHECK_CYCLE_ACTION = "com.salmoukas.cerberus.CHECK_CYCLE"
        const val CHECK_CYCLE_INTERVAL_MINUTES = 5
        const val CHECK_CYCLE_REFERENCE_SUCCESS_RATIO = 0.6
        const val CHECK_CYCLE_PURGE_OLDER_THAN_MINUTES = 60 * 24 * 30 // = 30 days

        // check status
        const val CHECK_STATUS_RETROGRADE_VALIDITY_MINUTES = CHECK_CYCLE_INTERVAL_MINUTES + 5
        const val CHECK_STATUS_REFRESH_INTERVAL_MINUTES = 1
        const val CHECK_STATUS_LATEST_PERIOD_MINUTES = 60 * 24 // = 1 day
        const val CHECK_STATUS_STALE_AFTER_MINUTES = CHECK_CYCLE_INTERVAL_MINUTES * 3
    }
}
