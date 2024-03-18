package com.simprints.core.tools.extentions

import android.database.Cursor
import androidx.core.database.getIntOrNull
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull

fun Cursor.getStringWithColumnName(columnName: String): String? {
    return this.getStringOrNull(this.getColumnIndex(columnName))
}

fun Cursor.getIntWithColumnName(columnName: String): Int? {
    return this.getIntOrNull(this.getColumnIndex(columnName))
}

fun Cursor.getLongWithColumnName(columnName: String): Long? {
    return this.getLongOrNull(this.getColumnIndex(columnName))
}
