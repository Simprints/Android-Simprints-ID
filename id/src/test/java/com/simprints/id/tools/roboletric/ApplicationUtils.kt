package com.simprints.id.tools.roboletric

import android.content.Context
import android.content.SharedPreferences
import com.simprints.id.data.prefs.PreferencesManagerImpl
import org.robolectric.RuntimeEnvironment

inline fun getRoboSharedPreferences(fileName: String = PreferencesManagerImpl.PREF_FILE_NAME): SharedPreferences {
    return RuntimeEnvironment.application.getSharedPreferences(fileName, Context.MODE_PRIVATE)
}
