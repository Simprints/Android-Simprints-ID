package com.simprints.id.services

import com.simprints.id.domain.moduleapi.app.requests.AppIdentityConfirmationRequest

interface GuidSelectionManager {
    suspend fun handleIdentityConfirmationRequest(request: AppIdentityConfirmationRequest)
}

