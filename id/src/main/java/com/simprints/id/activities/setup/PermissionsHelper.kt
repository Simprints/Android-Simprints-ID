package com.simprints.id.activities.setup

import android.Manifest
import com.simprints.id.orchestrator.steps.core.requests.SetupPermission
import com.simprints.id.orchestrator.steps.core.requests.SetupRequest

object PermissionsHelper{

internal fun extractPermissionsFromRequest(setupRequest: SetupRequest): List<String> =
        setupRequest.requiredPermissions.map {
            when (it) {
                SetupPermission.LOCATION -> Manifest.permission.ACCESS_FINE_LOCATION
            }
        }
}
