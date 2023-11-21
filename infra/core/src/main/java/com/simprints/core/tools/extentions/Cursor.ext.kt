package com.simprints.core.tools.extentions

import android.database.Cursor

fun Cursor.getStringWithColumnName(columnName: String): String? {
    return this.getString(this.getColumnIndex(columnName))
}

fun Cursor.getIntWithColumnName(columnName: String): Int {
    return this.getInt(this.getColumnIndex(columnName))
}
