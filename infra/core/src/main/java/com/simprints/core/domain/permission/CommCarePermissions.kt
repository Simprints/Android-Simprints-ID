package com.simprints.core.domain.permission

/**
 * Utility for CommCare permissions required to access CommCare content provider data.
 */
object CommCarePermissions {
    /**
     * Builds a dynamic CommCare permission string for a specific package.
     * @param callerPackageName The package name to build the permission for
     * @return The complete permission string in format "packageName.provider.cases.read"
     */
    fun buildPermissionForPackage(callerPackageName: String): String {
        return "$callerPackageName.provider.cases.read"
    }
}