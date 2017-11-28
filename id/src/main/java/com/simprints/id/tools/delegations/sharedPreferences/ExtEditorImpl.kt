package com.simprints.id.tools.delegations.sharedPreferences

import android.content.SharedPreferences
import com.simprints.id.model.Callout
import com.simprints.libdata.tools.Constants

class ExtEditorImpl(private val editor: SharedPreferences.Editor)
    : ExtSharedPreferences.Editor,
        SharedPreferences.Editor by editor {

    override fun <T: Enum<T>> putEnum(key: String, value: Enum<T>): ExtSharedPreferences.Editor {
        putString(key, value.name)
        return this
    }

    override fun <T: Any> putAny(key: String, value: T): ExtSharedPreferences.Editor {
        when (value) {
            is Long -> putLong(key, value)
            is String -> putString(key, value)
            is Int -> putInt(key, value)
            is Boolean -> putBoolean(key, value)
            is Float -> putFloat(key, value)
            is Constants.GROUP -> putEnum(key, value)
            is Callout -> putEnum(key, value)
            else -> throw IllegalArgumentException("Unsupported type")
        }
        return this
    }
}