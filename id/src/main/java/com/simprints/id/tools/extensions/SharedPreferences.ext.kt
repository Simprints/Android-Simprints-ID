package com.simprints.id.tools.extensions

import android.content.SharedPreferences

inline fun <reified T: Enum<T>> SharedPreferences.getEnum(key: String, defValue: Enum<T>): Enum<T> {
    val ordinal = getInt(key, -1)
    return if (enumValues<T>().indices.contains(ordinal)) {
        enumValues<T>()[ordinal]
    } else {
        defValue
    }
}

inline fun <reified T: Enum<T>> SharedPreferences.Editor.putEnum(key: String, value: Enum<T>): SharedPreferences.Editor {
    putInt(key, value.ordinal)
    return this
}