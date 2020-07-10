package com.simprints.id.domain.moduleapi.core.requests

import com.simprints.id.domain.modality.Modality
import com.simprints.id.orchestrator.steps.core.requests.CoreRequest
import kotlinx.android.parcel.Parcelize

@Parcelize
class SetupRequest(val modalitiesRequired: List<Modality>,
                   val requiredPermissions: List<SetupPermission>): CoreRequest

enum class SetupPermission {
    LOCATION
}
