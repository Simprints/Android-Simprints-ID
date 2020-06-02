package com.simprints.id.domain.moduleapi.core.response

import com.simprints.id.orchestrator.steps.core.response.CoreResponse
import com.simprints.id.orchestrator.steps.core.response.CoreResponseType
import kotlinx.android.parcel.Parcelize

@Parcelize
class SetupResponse: CoreResponse(type = CoreResponseType.SETUP)
