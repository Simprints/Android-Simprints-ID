package com.simprints.infra.projectsecuritystore.securitystate.repo.local

import android.content.Context
import com.simprints.infra.projectsecuritystore.securitystate.models.SecurityState
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal class SecurityStateLocalDataSource @Inject constructor(
    @ApplicationContext context: Context
) {

    // TODO Move data to an encrypted version - CORE-2590
    private val prefs = context.getSharedPreferences(PREF_FILE_NAME, PREF_MODE)

    var securityStatus: SecurityState.Status
        get() {
            val name = prefs.getString(KEY_SECURITY_STATUS, DEFAULT_SECURITY_STATUS)
                ?: DEFAULT_SECURITY_STATUS
            return SecurityState.Status.valueOf(name)
        }
        set(value) {
            prefs.edit().putString(KEY_SECURITY_STATUS, value.name).apply()
        }

    companion object {
        const val KEY_SECURITY_STATUS = "SecurityStatus"
        val DEFAULT_SECURITY_STATUS = SecurityState.Status.RUNNING.name
        const val PREF_FILE_NAME = "b3f0cf9b-4f3f-4c5b-bf85-7b1f44eddd7a"
        const val PREF_MODE = Context.MODE_PRIVATE
    }

}
