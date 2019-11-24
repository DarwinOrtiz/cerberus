package com.salmoukas.cerberus

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import com.salmoukas.cerberus.db.CheckResult
import java.io.BufferedReader
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
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (intent?.extras?.getBoolean("runCheckCycle") == true && worker == null) {
            // ensure job can be executed properly
            val wl = (getSystemService(Context.POWER_SERVICE) as PowerManager)
                .newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK,
                    Constants.WAKE_LOCK_MAIN_SERVICE
                )
                .apply {
                    acquire(120 * 1000)
                }

            // run worker
            worker = Thread {
                try {
                    checkCycleWork()
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
                            error = e.message
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
                        skip = (referenceSucceeded / referenceTotal.toDouble()) < Constants.CHECK_CYCLE_REQUIRED_REFERENCE_SUCCESS
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
                    Constants.WAKE_LOCK_MAIN_SERVICE
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
