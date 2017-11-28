package com.simprints.id.tools.delegations.sharedPreferences

import android.annotation.SuppressLint
import android.content.SharedPreferences
import com.simprints.id.model.Callout
import com.simprints.libdata.tools.Constants

class ExtSharedPreferencesImpl(private val prefs: SharedPreferences)
    : ExtSharedPreferences,
        SharedPreferences by prefs {

    override fun <T: Enum<T>> getEnum(key: String, defValue: T): T {
        return java.lang.Enum.valueOf(defValue.javaClass, getString(key, defValue.name))
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T: Any> getAny(key: String, defValue: T): T =
            when (defValue) {
                is Long -> getLong(key, defValue) as T
                is String -> getString(key, defValue) as T
                is Int -> getInt(key, defValue) as T
                is Boolean -> getBoolean(key, defValue) as T
                is Float -> getFloat(key, defValue) as T
                is Constants.GROUP -> getEnum(key, defValue) as T
                is Callout -> getEnum(key, defValue) as T
                else -> throw IllegalArgumentException("Unsupported type")
            }

    @SuppressLint("CommitPrefEdits")
    override fun edit(): ExtSharedPreferences.Editor = ExtEditorImpl(prefs.edit())

}

