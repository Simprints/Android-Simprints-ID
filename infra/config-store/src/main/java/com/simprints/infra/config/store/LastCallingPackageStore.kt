package com.simprints.infra.config.store

import android.content.Context
import androidx.core.content.edit
import com.simprints.infra.security.SecurityManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LastCallingPackageStore @Inject constructor(
    @ApplicationContext private val context: Context,
    securityManager: SecurityManager,
) {
    private val prefs = securityManager.buildEncryptedSharedPreferences(PREF_FILE_NAME)

    var lastCallingPackageName: String?
        get() = prefs.getString(
            KEY_LAST_CALLING_PACKAGE_NAME,
            context.getString(com.simprints.infra.resources.R.string.default_commcare_package_name)
        )
        set(value) {
            prefs.edit {
                if (value == null) {
                    remove(KEY_LAST_CALLING_PACKAGE_NAME)
                } else {
                    putString(KEY_LAST_CALLING_PACKAGE_NAME, value)
                }
            }
        }

    companion object {
        private const val PREF_FILE_NAME = "last_calling_package_store"
        private const val KEY_LAST_CALLING_PACKAGE_NAME = "last_calling_package_name"
    }
}