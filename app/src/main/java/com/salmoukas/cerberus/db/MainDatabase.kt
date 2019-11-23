package com.salmoukas.cerberus.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [CheckConfig::class, CheckResult::class], version = 1)
abstract class MainDatabase : RoomDatabase() {
    abstract fun checkConfigDao(): CheckConfigDao
    abstract fun checkResultDao(): CheckResultDao
}
