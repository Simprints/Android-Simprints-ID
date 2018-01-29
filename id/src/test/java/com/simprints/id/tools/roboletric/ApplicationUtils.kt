package com.simprints.id.tools.roboletric

import android.content.Context
import android.content.SharedPreferences
import com.simprints.id.data.prefs.PreferencesManagerImpl
import org.robolectric.RuntimeEnvironment

/**
 * Created by fabiotuzza on 29/01/2018.
 */

inline fun getRoboSharedPreferences(fileName: String = PreferencesManagerImpl.PREF_FILE_NAME): SharedPreferences {
    return RuntimeEnvironment.application.getSharedPreferences(PreferencesManagerImpl.PREF_FILE_NAME, Context.MODE_PRIVATE)
}
