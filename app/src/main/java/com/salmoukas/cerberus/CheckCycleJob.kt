package com.salmoukas.cerberus

import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.util.Log
import com.salmoukas.cerberus.db.CheckResult
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Executors


class CheckCycleJob : JobService() {

    @Volatile
    private var thread: Thread? = null

    @Volatile
    private var cancelThread: Boolean = false

    override fun onStartJob(params: JobParameters?): Boolean {

        // just to be sure, cancel any other running thread (should not happen?)
        cancelThread = true
        thread?.join()

        // setup worker thread
        cancelThread = false
        thread = Thread {

            Log.i("WORKER", "got some work to do")

            val db = (application as ThisApplication).db!!

            val timestamp = System.currentTimeMillis();
            val checks = db.checkConfigDao().all()

            val threadPool = Executors.newCachedThreadPool()
            try {
                checks.map {
                    threadPool.submit(Callable<CheckResult> {
                        var statusCode = 0
                        var content = ""
                        var error: String? = null
                        try {
                            val request = URL(it.url).openConnection() as HttpURLConnection
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
                            0,
                            timestamp,
                            it.uid,
                            if (it.statusCode != null) statusCode == it.statusCode else null,
                            if (it.contentMatch != null) content.contains(it.contentMatch) else null,
                            error,
                            false
                        ).let {
                            it.copy(
                                succeeded = (it.status_code_ok == null || it.status_code_ok)
                                        && (it.content_ok == null || it.content_ok)
                                        && it.error_message == null
                            )
                        }
                    })
                }.onEach {
                    val result = it.get()
                    Log.d("CHECK_CONFIG", db.checkConfigDao().byUid(result.configUid).toString())
                    Log.d("CHECK_RESULT", result.toString())
                    db.checkResultDao().insert(result)
                }
            } finally {
                threadPool.shutdown()
            }

            // signal completion
            jobFinished(params, false)
            thread = null

            // schedule next job
            if (!cancelThread) {
                scheduleJob(this)
            }
        }
        thread?.start()

        // job continues to run
        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {

        // cancel and join running worker thread
        cancelThread = true
        thread?.join()

        // do not retry job
        return false
    }


    companion object {
        fun scheduleJob(context: Context) {

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
            (context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler)
                .schedule(
                    JobInfo.Builder(
                        Constants.JOB_ID_CHECK_CYCLE,
                        ComponentName(context, CheckCycleJob::class.java)
                    )
                        .setMinimumLatency(delay)
                        .setOverrideDeadline(delay)
                        .build()
                )
        }

        fun cancelJob(context: Context) {

            // cancel running and pending check cycle jobs
            (context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler).cancel(
                Constants.JOB_ID_CHECK_CYCLE
            )
        }
    }
}
