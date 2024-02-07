package com.simprints.core.tools.extentions

import android.database.Cursor
import androidx.core.database.getLongOrNull

fun Cursor.getStringWithColumnName(columnName: String): String? {
    return this.getString(this.getColumnIndex(columnName))
}

fun Cursor.getIntWithColumnName(columnName: String): Int {
    return this.getInt(this.getColumnIndex(columnName))
}

fun Cursor.getLongWithColumnName(columnName: String): Long? {
    return this.getLongOrNull(this.getColumnIndex(columnName))
}
