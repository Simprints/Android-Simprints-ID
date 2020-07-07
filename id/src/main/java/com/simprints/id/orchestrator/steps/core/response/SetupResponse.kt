package com.simprints.id.orchestrator.steps.core.response

import com.simprints.id.orchestrator.steps.core.response.CoreResponse
import com.simprints.id.orchestrator.steps.core.response.CoreResponseType
import kotlinx.android.parcel.Parcelize

@Parcelize
class SetupResponse(val isSetupComplete: Boolean): CoreResponse(type = CoreResponseType.SETUP)
