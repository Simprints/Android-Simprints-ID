package com.simprints.feature.clientapi.usecases

import com.simprints.infra.events.EventRepository
import javax.inject.Inject

internal class GetCurrentSessionIdUseCase @Inject constructor(
    private val eventRepository: EventRepository,
) {

    suspend operator fun invoke(): String = eventRepository.getCurrentCaptureSessionEvent().id

}
