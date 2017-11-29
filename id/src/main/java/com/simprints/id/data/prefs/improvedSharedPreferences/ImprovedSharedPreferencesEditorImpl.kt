package com.simprints.id.data.prefs.improvedSharedPreferences

import android.content.SharedPreferences

/**
 * @author: Etienne Thiery (etienne@simprints.com)
 */
class ImprovedSharedPreferencesEditorImpl(private val editor: SharedPreferences.Editor)
    : ImprovedSharedPreferences.Editor,
        SharedPreferences.Editor by editor {

    override fun <T: Any> putAny(key: String, value: T): ImprovedSharedPreferences.Editor {
        when (value) {
            is Long -> putLong(key, value)
            is String -> putString(key, value)
            is Int -> putInt(key, value)
            is Boolean -> putBoolean(key, value)
            is Float -> putFloat(key, value)
            else -> throw IllegalArgumentException("Unsupported type")
        }
        return this
    }
}