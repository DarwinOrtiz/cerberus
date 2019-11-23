package com.salmoukas.cerberus.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface CheckResultDao {
    @Query("SELECT * FROM check_result WHERE timestamp_utc >= (strftime('%s','now') - :window)")
    fun latestLive(window: Long): LiveData<List<CheckResult>>

    @Insert
    fun insert(checkResult: CheckResult): Long
}
