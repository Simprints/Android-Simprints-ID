package com.simprints.feature.logincheck.usecases

import com.simprints.feature.orchestrator.models.ActionRequest
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.security.SecurityManager
import javax.inject.Inject

internal class IsUserSignedInUseCase @Inject constructor(
    private val authStore: AuthStore,
    private val secureDataManager: SecurityManager,
) {

    operator fun invoke(action: ActionRequest): SignedInState {
        val signedInProjectId = authStore.signedInProjectId

        if (signedInProjectId.isEmpty()) {
            return SignedInState.NOT_SIGNED_IN
        }

        if (signedInProjectId != action.projectId) {
            return SignedInState.MISMATCHED_PROJECT_ID
        }

        // Check if the local key is valid
        try {
            secureDataManager.getLocalDbKeyOrThrow(signedInProjectId)
        } catch (t: Throwable) {
            return SignedInState.NOT_SIGNED_IN
        }

        // Check if firebase app is valid
        if (!authStore.isFirebaseSignedIn(signedInProjectId)) {
            return SignedInState.NOT_SIGNED_IN
        }

        return SignedInState.SIGNED_IN
    }

    enum class SignedInState {
        SIGNED_IN,
        NOT_SIGNED_IN,
        MISMATCHED_PROJECT_ID,
    }
}
