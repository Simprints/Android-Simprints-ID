package com.simprints.id.tools.extensions

import android.content.SharedPreferences

inline fun <reified T: Enum<T>> SharedPreferences.getEnum(key: String, defValue: Enum<T>): Enum<T> {
    val ordinal = getInt(key, -1)
    if (enumValues<T>().indices.contains(ordinal)) {
        return enumValues<T>()[ordinal]
    } else {
        return defValue
    }
}

inline fun <reified T: Enum<T>> SharedPreferences.Editor.putEnum(key: String, value: Enum<T>): SharedPreferences.Editor {
    putInt(key, value.ordinal)
    return this
}