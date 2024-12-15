package com.simprints.feature.logincheck.usecases

import com.simprints.core.tools.time.TimeHelper
import com.simprints.infra.events.event.domain.models.AuthorizationEvent
import com.simprints.infra.events.session.SessionEventRepository
import com.simprints.infra.orchestration.data.ActionRequest
import javax.inject.Inject

internal class AddAuthorizationEventUseCase @Inject constructor(
    private val coreEventRepository: SessionEventRepository,
    private val timeHelper: TimeHelper,
) {
    suspend operator fun invoke(
        request: ActionRequest,
        authorized: Boolean,
    ) {
        val userInfo = request
            .takeIf { authorized }
            ?.let { AuthorizationEvent.AuthorizationPayload.UserInfo(it.projectId, it.userId) }
        val result = if (authorized) AuthorizationEvent.AuthorizationPayload.AuthorizationResult.AUTHORIZED else AuthorizationEvent.AuthorizationPayload.AuthorizationResult.NOT_AUTHORIZED

        coreEventRepository.addOrUpdateEvent(AuthorizationEvent(timeHelper.now(), result, userInfo))
    }
}
