package com.simprints.core.sharedpreferences


interface PreferencesManager {

    fun <T> getSharedPreference(key: String, defaultValue: T): T

    fun setSharedPreference(key: String, value: Any)

    fun clearAllSharedPreferences()
}
