package com.simprints.id.services

import com.simprints.id.orchestrator.steps.core.requests.GuidSelectionRequest

interface GuidSelectionManager {
    suspend fun handleIdentityConfirmationRequest(request: GuidSelectionRequest)
}

