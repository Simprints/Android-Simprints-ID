package com.simprints.id.services

import com.simprints.id.domain.moduleapi.app.requests.AppIdentityConfirmationRequest
import io.reactivex.Completable

interface GuidSelectionManager {
    fun saveGUIDSelection(request: AppIdentityConfirmationRequest): Completable
}
