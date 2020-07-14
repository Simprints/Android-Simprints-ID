package com.simprints.id.orchestrator.steps.core.requests

import com.simprints.id.domain.modality.Modality
import kotlinx.android.parcel.Parcelize

@Parcelize
class SetupRequest(val modalitiesRequired: List<Modality>,
                   val requiredPermissions: List<SetupPermission>): CoreRequest

enum class SetupPermission {
    LOCATION
}
