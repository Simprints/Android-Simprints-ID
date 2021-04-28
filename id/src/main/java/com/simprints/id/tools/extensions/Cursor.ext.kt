package com.simprints.id.tools.extensions

import android.database.Cursor

fun Cursor.getStringWithColumnName(columnName: String): String? {
    return this.getString(this.getColumnIndex(columnName))
}
