package com.simprints.feature.clientapi.usecases

import com.simprints.infra.events.session.SessionEventRepository
import javax.inject.Inject

internal class GetCurrentSessionIdUseCase @Inject constructor(
    private val sessionEventRepository: SessionEventRepository,
) {
    suspend operator fun invoke(): String = sessionEventRepository.getCurrentSessionScope().id
}
