package com.salmoukas.cerberus.db

import androidx.room.*

@Dao
interface CheckResultDao {
    @Query("SELECT * FROM check_result")
    fun all(): List<CheckResult>

    @Query("SELECT * FROM check_result WHERE uid = :uid")
    fun byUid(uid: Long): CheckResult

    @Insert
    fun insert(checkResult: CheckResult): Long

    @Insert
    fun insert(vararg checkResults: CheckResult): List<Long>

    @Update
    fun update(checkResult: CheckResult): Int

    @Delete
    fun delete(checkResult: CheckResult)
}
