package com.simprints.feature.clientapi.session

import com.simprints.infra.projectsecuritystore.SecurityStateRepository
import com.simprints.infra.projectsecuritystore.securitystate.models.SecurityState
import javax.inject.Inject

internal class GetProjectStateUseCase @Inject constructor(
    private val securityStateRepository: SecurityStateRepository,
) {

    operator fun invoke(): ProjectState {
        // Finally make sure that project is still active
        val status = securityStateRepository.getSecurityStatusFromLocal()
        return when {
            status == SecurityState.Status.PROJECT_PAUSED -> ProjectState.PAUSED
            status == SecurityState.Status.PROJECT_ENDING -> ProjectState.ENDING
            status.isCompromisedOrProjectEnded() -> ProjectState.ENDED
            else -> ProjectState.ACTIVE
        }
    }

    enum class ProjectState {
        ACTIVE,
        PAUSED,
        ENDING,
        ENDED,
    }
}
