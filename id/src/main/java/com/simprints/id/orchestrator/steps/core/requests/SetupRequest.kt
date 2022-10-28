package com.simprints.id.orchestrator.steps.core.requests

import com.simprints.infra.config.domain.models.GeneralConfiguration
import kotlinx.parcelize.Parcelize

@Parcelize
class SetupRequest(val modalitiesRequired: List<GeneralConfiguration.Modality>,
                   val requiredPermissions: List<SetupPermission>): CoreRequest

enum class SetupPermission {
    LOCATION
}
