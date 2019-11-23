package com.salmoukas.cerberus.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface CheckConfigDao {
    @Query("SELECT * FROM check_config")
    fun all(): List<CheckConfig>

    @Query("SELECT * FROM check_config WHERE is_reference = 0")
    fun targetsLive(): LiveData<List<CheckConfig>>

    @Insert
    fun insert(vararg checkConfigs: CheckConfig): List<Long>
}
