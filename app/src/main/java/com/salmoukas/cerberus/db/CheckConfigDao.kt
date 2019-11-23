package com.salmoukas.cerberus.db

import androidx.room.*

@Dao
interface CheckConfigDao {
    @Query("SELECT * FROM check_config")
    fun all(): List<CheckConfig>

    @Query("SELECT * FROM check_config WHERE is_reference <> 0")
    fun references(): List<CheckConfig>

    @Query("SELECT * FROM check_config WHERE is_reference = 0")
    fun targets(): List<CheckConfig>

    @Query("SELECT * FROM check_config WHERE uid = :uid")
    fun byUid(uid: Long): CheckConfig

    @Insert
    fun insert(checkConfig: CheckConfig): Long

    @Insert
    fun insert(vararg checkConfigs: CheckConfig): List<Long>

    @Update
    fun update(checkConfig: CheckConfig): Int

    @Delete
    fun delete(checkConfig: CheckConfig)
}
