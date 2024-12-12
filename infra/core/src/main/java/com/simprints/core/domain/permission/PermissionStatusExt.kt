package com.simprints.core.domain.permission

fun List<PermissionStatus>.worstPermissionStatus() = when {
    contains(PermissionStatus.DeniedNeverAskAgain) -> PermissionStatus.DeniedNeverAskAgain
    contains(PermissionStatus.Denied) -> PermissionStatus.Denied
    else -> PermissionStatus.Granted
}
