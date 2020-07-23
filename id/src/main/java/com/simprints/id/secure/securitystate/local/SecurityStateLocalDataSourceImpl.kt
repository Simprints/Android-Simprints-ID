package com.simprints.id.secure.securitystate.local

import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences
import com.simprints.id.secure.models.SecurityState

class SecurityStateLocalDataSourceImpl(
    private val prefs: ImprovedSharedPreferences
) : SecurityStateLocalDataSource {

    override var securityStatus: SecurityState.Status
        get() {
            val name = prefs.getString(KEY_SECURITY_STATUS, DEFAULT_SECURITY_STATUS)
            return SecurityState.Status.valueOf(name)
        }
        set(value) {
            prefs.edit().putPrimitive(KEY_SECURITY_STATUS, value.name).commit()
        }

    companion object {
        const val KEY_SECURITY_STATUS = "SecurityStatus"
        val DEFAULT_SECURITY_STATUS = SecurityState.Status.RUNNING.name
    }

}
