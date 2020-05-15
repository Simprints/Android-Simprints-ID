package com.simprints.id.domain.moduleapi.core.requests

import kotlinx.android.parcel.Parcelize

@Parcelize
class SetupRequest(val requiredPermissions: List<SetupPermission>): CoreRequest

enum class SetupPermission {
    LOCATION
}
