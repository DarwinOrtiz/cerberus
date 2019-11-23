package com.salmoukas.cerberus.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "check_config")
data class CheckConfig(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "uid") val uid: Long,
    @ColumnInfo(name = "is_reference") val isReference: Boolean,
    @ColumnInfo(name = "url") val url: String,
    @ColumnInfo(name = "status_code") val statusCode: Int?,
    @ColumnInfo(name = "content_match") val contentMatch: String?
)
