package com.simprints.feature.clientapi.usecases

import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.callout.EnrolmentCalloutEvent
import com.simprints.infra.events.event.domain.models.callout.IdentificationCalloutEvent
import kotlinx.coroutines.flow.toList
import javax.inject.Inject

internal class IsCurrentSessionAnIdentificationOrEnrolmentUseCase @Inject constructor(
        private val eventRepository: EventRepository,
) {

    suspend operator fun invoke(): Boolean = eventRepository
        .getCurrentCaptureSessionEvent()
        .let { eventRepository.observeEventsFromSession(it.id) }
        .toList()
        .any { it is IdentificationCalloutEvent || it is EnrolmentCalloutEvent }

}
