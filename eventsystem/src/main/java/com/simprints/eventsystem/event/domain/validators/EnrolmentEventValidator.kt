package com.simprints.eventsystem.event.domain.validators

import com.simprints.eventsystem.event.domain.models.EnrolmentEventV2
import com.simprints.eventsystem.event.domain.models.Event
import com.simprints.eventsystem.event.domain.models.PersonCreationEvent
import com.simprints.eventsystem.event.domain.models.face.FaceCaptureEventV3
import com.simprints.eventsystem.event.domain.models.fingerprint.FingerprintCaptureEventV3
import com.simprints.eventsystem.exceptions.validator.EnrolmentEventValidatorException

class EnrolmentEventValidator : EventValidator {

    /**
     * In order to create an enrolment event we need to already have a biometric capture event
     * and a person creation event. Without these the backend can not create a subject.
     */
    override fun validate(currentEvents: List<Event>, eventToAdd: Event) {
        if (eventToAdd is EnrolmentEventV2) {
            val hasFingerprint = currentEvents.any { it is FingerprintCaptureEventV3 }
            val hasFace = currentEvents.any { it is FaceCaptureEventV3 }
            val hasPersonCreation = currentEvents.any { it is PersonCreationEvent }

            if (!hasFingerprint && !hasFace)
                throw EnrolmentEventValidatorException()

            if (!hasPersonCreation)
                throw EnrolmentEventValidatorException()
        }
    }

}

