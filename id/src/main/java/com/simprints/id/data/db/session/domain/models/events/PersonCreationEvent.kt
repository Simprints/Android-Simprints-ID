package com.simprints.id.data.db.session.domain.models.events

import androidx.annotation.Keep
import com.simprints.core.tools.EncodingUtils
import com.simprints.id.data.db.person.domain.FingerprintSample
import com.simprints.id.data.db.session.domain.models.session.SessionEvents
import com.simprints.id.tools.TimeHelper

// At the end of the sequence of capture, we build a Person object used either for enrolment or verification/identification
@Keep
class PersonCreationEvent(starTime: Long,
                          val fingerprintCaptureIds: List<String>) : Event(EventType.PERSON_CREATION, starTime) {

    companion object {
        fun build(timeHelper: TimeHelper,
                  currentSession: SessionEvents,
                  fingerprintSamples: List<FingerprintSample>) =
            PersonCreationEvent(
                timeHelper.now(),
                extractCaptureEventIdsBasedOnPersonTemplate(currentSession, fingerprintSamples.map { EncodingUtils.byteArrayToBase64(it.template) })
            )

        // It extracts CaptureEvents Ids with the templates used to create the "Person" object for
        // identification, verification, enrolment.
        private fun extractCaptureEventIdsBasedOnPersonTemplate(sessionEvents: SessionEvents, personTemplates: List<String>): List<String> =
            sessionEvents.events
                .filterIsInstance(FingerprintCaptureEvent::class.java)
                .filter {
                    it.fingerprint?.template in personTemplates && it.result != FingerprintCaptureEvent.Result.SKIPPED
                }
                .map { it.id }
    }
}
