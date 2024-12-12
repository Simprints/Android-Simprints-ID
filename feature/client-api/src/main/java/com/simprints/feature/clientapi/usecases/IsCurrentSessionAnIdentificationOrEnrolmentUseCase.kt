package com.simprints.feature.clientapi.usecases

import com.simprints.infra.events.session.SessionEventRepository
import com.simprints.infra.events.event.domain.models.callout.EnrolmentCalloutEvent
import com.simprints.infra.events.event.domain.models.callout.IdentificationCalloutEvent
import javax.inject.Inject

internal class IsCurrentSessionAnIdentificationOrEnrolmentUseCase @Inject constructor(
    private val eventRepository: SessionEventRepository,
) {

    suspend operator fun invoke(): Boolean = eventRepository
        .getEventsInCurrentSession()
        .any { it is IdentificationCalloutEvent || it is EnrolmentCalloutEvent }

}
