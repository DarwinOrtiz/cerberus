package com.salmoukas.cerberus.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "check_result")
data class CheckResult(
    @PrimaryKey(autoGenerate = true) val uid: Long,
    @ColumnInfo(name = "timestamp_utc") val timestampUtc: Long,
    @ColumnInfo(name = "config_uid") val configUid: Long,
    @ColumnInfo(name = "status_code_ok") val status_code_ok: Boolean?,
    @ColumnInfo(name = "content_ok") val content_ok: Boolean?,
    @ColumnInfo(name = "error_message") val error_message: String?,
    @ColumnInfo(name = "succeeded") val succeeded: Boolean
)
