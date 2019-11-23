package com.salmoukas.cerberus.db

import androidx.room.*

@Entity(
    tableName = "check_result",
    foreignKeys = [ForeignKey(
        entity = CheckConfig::class,
        parentColumns = ["uid"],
        childColumns = ["config_uid"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.RESTRICT
    )],
    indices = [
        Index(value = ["config_uid"]),
        Index(value = ["timestamp_utc"])
    ]
)
data class CheckResult(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "uid") val uid: Long,
    @ColumnInfo(name = "timestamp_utc") val timestampUtc: Long,
    @ColumnInfo(name = "config_uid") val configUid: Long,
    @ColumnInfo(name = "status_code_ok") val status_code_ok: Boolean?,
    @ColumnInfo(name = "content_ok") val content_ok: Boolean?,
    @ColumnInfo(name = "error_message") val error_message: String?,
    @ColumnInfo(name = "succeeded") val succeeded: Boolean,
    @ColumnInfo(name = "skip") val skip: Boolean
)
