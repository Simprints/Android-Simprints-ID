package com.simprints.feature.clientapi.activity.usecases

import com.simprints.feature.clientapi.models.ActionRequest
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.projectsecuritystore.SecurityStateRepository
import com.simprints.infra.projectsecuritystore.securitystate.models.SecurityState
import com.simprints.infra.security.SecurityManager
import javax.inject.Inject

internal class IsSignedToActiveProjectUseCase @Inject constructor(
    private val authStore: AuthStore,
    private val secureDataManager: SecurityManager,
    private val securityStateRepository: SecurityStateRepository,
) {

    operator fun invoke(action: ActionRequest): SignedInState {
        if (authStore.signedInProjectId.isEmpty()) {
            return SignedInState.NOT_SIGNED_IN
        }
        val signedInProjectId = authStore.signedInProjectId

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
        if (!authStore.isFirebaseSignedIn(authStore.signedInProjectId)) {
            return SignedInState.NOT_SIGNED_IN
        }

        // Finally make sure that project is still active
        val status = securityStateRepository.getSecurityStatusFromLocal()
        return when {
            status == SecurityState.Status.PROJECT_PAUSED -> SignedInState.PROJECT_PAUSED
            status == SecurityState.Status.PROJECT_ENDING -> SignedInState.PROJECT_ENDING
            status.isCompromisedOrProjectEnded() -> SignedInState.NOT_SIGNED_IN
            else -> SignedInState.SIGNED_IN
        }
    }

    enum class SignedInState {
        SIGNED_IN,
        NOT_SIGNED_IN,
        MISMATCHED_PROJECT_ID,
        PROJECT_PAUSED,
        PROJECT_ENDING,
    }
}
