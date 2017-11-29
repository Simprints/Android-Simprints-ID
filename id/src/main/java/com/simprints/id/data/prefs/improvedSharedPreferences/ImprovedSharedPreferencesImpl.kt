package com.simprints.id.data.prefs.improvedSharedPreferences

import android.annotation.SuppressLint
import android.content.SharedPreferences

/**
 * @author: Etienne Thiery (etienne@simprints.com)
 */
class ImprovedSharedPreferencesImpl(private val prefs: SharedPreferences)
    : ImprovedSharedPreferences,
        SharedPreferences by prefs {

    @Suppress("UNCHECKED_CAST")
    override fun <T: Any> getAny(key: String, defValue: T): T =
            when (defValue) {
                is Long -> getLong(key, defValue) as T
                is String -> getString(key, defValue) as T
                is Int -> getInt(key, defValue) as T
                is Boolean -> getBoolean(key, defValue) as T
                is Float -> getFloat(key, defValue) as T
                else -> throw IllegalArgumentException("Unsupported type")
            }

    @SuppressLint("CommitPrefEdits")
    override fun edit(): ImprovedSharedPreferences.Editor = ImprovedSharedPreferencesEditorImpl(prefs.edit())

}

