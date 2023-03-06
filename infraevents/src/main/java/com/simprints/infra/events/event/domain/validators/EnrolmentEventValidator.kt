package com.simprints.infra.events.domain.validators

import com.simprints.infra.events.event.domain.models.EnrolmentEventV2
import com.simprints.infra.events.event.domain.models.Event
import com.simprints.infra.events.event.domain.models.PersonCreationEvent
import com.simprints.infra.events.event.domain.models.face.FaceCaptureEvent
import com.simprints.infra.events.event.domain.models.fingerprint.FingerprintCaptureEvent
import com.simprints.infra.events.exceptions.validator.EnrolmentEventValidatorException

internal class EnrolmentEventValidator : EventValidator {

    /**
     * In order to create an enrolment event we need to already have a biometric capture event
     * and a person creation event. Without these the backend can not create a subject.
     */
    override fun validate(currentEvents: List<Event>, eventToAdd: Event) {
        if (eventToAdd is EnrolmentEventV2) {
            val hasFingerprint = currentEvents.any { it is FingerprintCaptureEvent }
            val hasFace = currentEvents.any { it is FaceCaptureEvent }
            val hasPersonCreation = currentEvents.any { it is PersonCreationEvent }

            if (!hasFingerprint && !hasFace)
                throw EnrolmentEventValidatorException("Missing fingerprint or face capture event")

            if (!hasPersonCreation)
                throw EnrolmentEventValidatorException("Missing person creation event")
        }
    }

}

