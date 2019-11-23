package com.salmoukas.cerberus

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import androidx.room.Room
import com.salmoukas.cerberus.db.CheckConfig
import com.salmoukas.cerberus.db.MainDatabase

class ThisApplication : Application() {

    var db: MainDatabase? = null; private set

    override fun onCreate() {
        super.onCreate()

        // create database instance
        db = Room.databaseBuilder(
            applicationContext,
            MainDatabase::class.java, "main"
        )
            .allowMainThreadQueries()
            .build()

        // TEST
        if (db!!.checkConfigDao().all().isEmpty()) {
            db!!.checkConfigDao().insert(
                CheckConfig(
                    0,
                    true,
                    "https://www.google.com/",
                    200,
                    null
                ),
                CheckConfig(
                    0,
                    true,
                    "https://www.bing.com/",
                    200,
                    null
                ),
                CheckConfig(
                    0,
                    true,
                    "https://www.yahoo.com/",
                    200,
                    null
                ),
                CheckConfig(
                    0,
                    false,
                    "https://kennstdudasbuch.de/",
                    200,
                    "<title>Das Kennst du das&#x2026;? Buch</title>"
                ),
                CheckConfig(
                    0,
                    false,
                    "https://mikripatrida.com/en",
                    200,
                    "<title>Greek Lifestyle, Places and Music for Greeks abroad!</title>"
                ),
                CheckConfig(
                    0,
                    false,
                    "https://assetninja.art/",
                    200,
                    "<title>Asset Ninja</title>"
                )
            )
        }

        // start main service (if not started)
        startForegroundService(Intent(this, MainService::class.java))

        // install keep alive broadcast
        (getSystemService(Context.ALARM_SERVICE) as AlarmManager).setInexactRepeating(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + Constants.INTERVAL_KEEP_ALIVE,
            Constants.INTERVAL_KEEP_ALIVE,
            PendingIntent.getBroadcast(this, 0, Intent(Constants.INTENT_KEEP_ALIVE), 0)
        )
    }
}
