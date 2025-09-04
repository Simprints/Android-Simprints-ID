package com.simprints.infra.eventsync.permission

import android.content.Context
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommCarePermissionChecker @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    /**
     * Checks if the required CommCare permissions are granted.
     * These permissions are needed to access CommCare content provider data.
     *
     * @return true if both CommCare permissions are granted, false otherwise
     */
    fun hasCommCarePermissions(): Boolean {
        val permissions = listOf(
            "org.commcare.dalvik.provider.cases.read",
            "org.commcare.dalvik.debug.provider.cases.read"
        )
        
        return permissions.any { permission ->
            context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
        }
    }
}