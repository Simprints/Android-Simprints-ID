package com.simprints.id.orchestrator.steps.core.response

import kotlinx.parcelize.Parcelize

@Parcelize
class SetupResponse(val isSetupComplete: Boolean): CoreResponse(type = CoreResponseType.SETUP)
