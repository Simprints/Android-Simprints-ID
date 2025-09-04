package com.simprints.infra.eventsync.permission

import android.content.Context
import android.content.pm.PackageManager
import com.simprints.core.domain.permission.CommCarePermissions
import com.simprints.infra.config.store.LastCallingPackageStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommCarePermissionChecker @Inject constructor(
    @ApplicationContext private val context: Context,
    private val lastCallingPackageStore: LastCallingPackageStore,
) {
    /**
     * Checks if the required CommCare permission is granted for the last calling package.
     *
     * @return true if the CommCare permission for the last calling package is granted, false otherwise
     */
    fun hasCommCarePermissions(): Boolean {
        val targetPermission = CommCarePermissions.buildPermissionForPackage(
            lastCallingPackageStore.lastCallingPackageName ?: "",
        )
        return context.checkSelfPermission(targetPermission) == PackageManager.PERMISSION_GRANTED
    }
}
