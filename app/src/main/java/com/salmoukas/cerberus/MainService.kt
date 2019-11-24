package com.salmoukas.cerberus

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import com.salmoukas.cerberus.db.CheckResult
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.concurrent.Callable
import java.util.concurrent.Executors

class MainService : Service() {

    @Volatile
    private var worker: Thread? = null

    @Volatile
    private var lastErrorNotification: Long? = null

    override fun onCreate() {
        super.onCreate()

        // create notification channels
        NotificationChannel(
            Constants.MONITORING_PRIMARY_NOTIFICATION_CHANNEL,
            resources.getString(R.string.app_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            enableVibration(false)
            enableLights(false)
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(this)
        }

        NotificationChannel(
            Constants.MONITORING_ERROR_NOTIFICATION_CHANNEL,
            resources.getString(R.string.app_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            enableVibration(true)
            enableLights(true)
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(this)
        }

        // create notification and post service to foreground
        startForeground(
            Constants.MONITORING_PRIMARY_NOTIFICATION_ID,
            buildPrimaryNotification(null, null)
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (intent?.extras?.getBoolean("runCheckCycle") == true && worker == null) {
            // ensure job can be executed properly
            val wl = (getSystemService(Context.POWER_SERVICE) as PowerManager)
                .newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK,
                    "cerberus:checkcycleworker"
                )
                .apply {
                    acquire(120 * 1000)
                }

            // run worker
            worker = Thread {
                try {
                    checkCycleWork()
                    deriveNotificationStatus()
                } finally {
                    MainReceiver.ctlScheduleCheckCycle(this)
                    wl.release()
                    worker = null
                }
            }
            worker!!.start()
        } else {
            MainReceiver.ctlScheduleCheckCycle(this)
        }

        return START_STICKY
    }

    private fun checkCycleWork() {

        Log.i("MAIN_SERVICE/WORKER", "running check cycle")

        val db = (application as ThisApplication).db!!

        val timestamp = System.currentTimeMillis() / 1000L
        val checks = db.checkConfigDao().all().map { it.uid to it }.toMap()

        var referenceTotal = 0
        var referenceSucceeded = 0

        val threadPool = Executors.newCachedThreadPool()
        try {
            checks.map {
                threadPool.submit(Callable<CheckResult> {
                    var statusCode = 0
                    var content = ""
                    var error: String? = null
                    try {
                        val request = URL(it.value.url).openConnection() as HttpURLConnection
                        try {
                            statusCode = request.responseCode
                            BufferedReader(
                                InputStreamReader(
                                    request.inputStream,
                                    StandardCharsets.UTF_8
                                )
                            ).use {
                                content = it.readText()
                            }
                        } catch (e: IOException) {
                            if (e !is FileNotFoundException) {
                                error = e.message
                            }
                            if (request.errorStream != null) {
                                BufferedReader(
                                    InputStreamReader(
                                        request.errorStream,
                                        StandardCharsets.UTF_8
                                    )
                                ).use {
                                    content = it.readText()
                                }
                            }
                        }
                    } catch (e: Exception) {
                        error = e.message
                    }
                    CheckResult(
                        uid = 0,
                        timestampUtc = timestamp,
                        configUid = it.key,
                        status_code_ok = if (it.value.statusCode != null) statusCode == it.value.statusCode!! else null,
                        content_ok = if (it.value.contentMatch != null) content.contains(it.value.contentMatch!!) else null,
                        error_message = error,
                        succeeded = false,
                        skip = false
                    )
                })
            }.map {
                it.get()
            }.map {
                it.copy(
                    succeeded = (it.status_code_ok == null || it.status_code_ok)
                            && (it.content_ok == null || it.content_ok)
                            && it.error_message == null
                )
            }.onEach {
                if (checks.getValue(it.configUid).isReference) {
                    referenceTotal++
                    if (it.succeeded) {
                        referenceSucceeded++
                    }
                }
            }.map {
                if (!checks.getValue(it.configUid).isReference) {
                    it.copy(
                        skip = (referenceSucceeded / referenceTotal.toDouble()) < Constants.CHECK_CYCLE_REFERENCE_SUCCESS_RATIO
                    )
                } else {
                    it
                }
            }.onEach {
                Log.d("MAIN_SERVICE/WORKER", "config = " + checks.getValue(it.configUid).toString())
                Log.d("MAIN_SERVICE/WORKER", "result = $it")
                db.checkResultDao().insert(it)
            }
        } finally {
            threadPool.shutdown()
        }
    }

    private fun deriveNotificationStatus() {
        val db = (application as ThisApplication).db!!

        val checkConfigs = db.checkConfigDao().targets()
        val checkResults = db.checkResultDao().period(Constants.CHECK_STATUS_PERIOD_SECONDS)

        var failedChecks = 0
        var staleChecks = 0

        val now = System.currentTimeMillis() / 1000L

        checkConfigs.onEach { cit ->
            checkResults.filter { rit -> cit.uid == rit.configUid && !rit.skip }
                .maxBy { it.timestampUtc }
                .let {
                    if (it != null && !it.succeeded) {
                        failedChecks += 1
                    }
                    if (it == null || (now - it.timestampUtc) >= Constants.CHECK_STATUS_STALE_AFTER_SECONDS) {
                        staleChecks += 1
                    }
                }
        }

        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .notify(
                Constants.MONITORING_PRIMARY_NOTIFICATION_ID,
                buildPrimaryNotification(failedChecks, staleChecks)
            )

        if (failedChecks > 0) {
            if (lastErrorNotification == null || (lastErrorNotification!! - System.currentTimeMillis()) > Constants.MONITORING_ERROR_NOTIFICATION_NOREPEAT_MILLISECONDS) {
                (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                    .notify(
                        Constants.MONITORING_ERROR_NOTIFICATION_ID,
                        buildErrorNotification(failedChecks, staleChecks)
                    )
                lastErrorNotification = System.currentTimeMillis()
            }
        } else {
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .cancel(Constants.MONITORING_ERROR_NOTIFICATION_ID)
            lastErrorNotification = null
        }
    }

    private fun buildNotificationText(failedChecks: Int?, staleChecks: Int?): String {
        return when {
            (failedChecks == null || staleChecks == null) -> resources.getString(R.string.notification_init)
            (failedChecks == 0 && staleChecks == 0) -> resources.getString(R.string.notification_ok)
            (failedChecks > 0 && staleChecks > 0) -> resources.getString(
                R.string.notification_error_failed_stale,
                failedChecks,
                staleChecks
            )
            (failedChecks > 0) -> resources.getString(
                R.string.notification_error_failed,
                failedChecks
            )
            else -> resources.getString(R.string.notification_error_stale, staleChecks)
        }
    }

    private fun buildPrimaryNotification(failedChecks: Int?, staleChecks: Int?): Notification {

        val text = buildNotificationText(failedChecks, staleChecks)

        val icon = when {
            (failedChecks == null || staleChecks == null) -> R.drawable.ic_launcher_foreground
            (failedChecks == 0 && staleChecks == 0) -> R.drawable.ic_launcher_foreground
            (failedChecks == 0 && staleChecks > 0) -> R.drawable.ic_launcher_foreground
            else -> R.drawable.ic_launcher_foreground
        }

        return Notification.Builder(this, Constants.MONITORING_PRIMARY_NOTIFICATION_CHANNEL)
            .setContentTitle(resources.getString(R.string.app_name))
            .setContentText(text)
            .setStyle(Notification.BigTextStyle().bigText(text))
            .setSmallIcon(icon)
            .setWhen(System.currentTimeMillis())
            .setShowWhen(true)
            .setContentIntent(
                PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), 0)
            )
            .setOngoing(true)
            .build()
    }

    private fun buildErrorNotification(failedChecks: Int, staleChecks: Int): Notification {

        val text = buildNotificationText(failedChecks, staleChecks)

        return Notification.Builder(this, Constants.MONITORING_ERROR_NOTIFICATION_CHANNEL)
            .setContentTitle(resources.getString(R.string.app_name))
            .setContentText(text)
            .setStyle(Notification.BigTextStyle().bigText(text))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setColor(Color.RED)
            .setColorized(true)
            .setWhen(System.currentTimeMillis())
            .setShowWhen(true)
            .setContentIntent(
                PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), 0)
            )
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()

        // join worker
        val w = worker
        try {
            w?.join()
        } catch (e: InterruptedException) {
        }

        // cancel pending check cycle
        MainReceiver.ctlCancelCheckCycle(this)

        // remove from foreground
        stopForeground(true)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    companion object {
        fun ctlStartService(context: Context, runCheckCycle: Boolean = false) {

            // ensure service can start properly
            (context.applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager)
                .newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK,
                    "cerberus:startmainservice"
                )
                .acquire(6 * 1000)

            // start main service (if not started)
            Log.d("MAIN_SERVICE", if (runCheckCycle) "run check cycle" else "start service")
            context.applicationContext.startForegroundService(
                Intent(
                    context.applicationContext,
                    MainService::class.java
                ).putExtra("runCheckCycle", runCheckCycle)
            )
        }

        fun ctlRunCheckCycle(context: Context) {
            ctlStartService(context, true)
        }
    }
}
