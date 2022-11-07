package com.simprints.id.services.guidselection

import com.simprints.id.orchestrator.steps.core.requests.GuidSelectionRequest

interface GuidSelectionManager {
    fun handleConfirmIdentityRequest(request: GuidSelectionRequest)
}

