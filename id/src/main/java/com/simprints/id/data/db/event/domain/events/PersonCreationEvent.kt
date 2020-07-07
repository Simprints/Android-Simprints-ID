package com.simprints.id.data.db.event.domain.events

import androidx.annotation.Keep
import com.simprints.core.tools.EncodingUtils
import com.simprints.id.data.db.event.domain.session.SessionEvents
import com.simprints.id.data.db.subject.domain.FingerprintSample
import com.simprints.id.tools.TimeHelper
import java.util.*

@Keep
class PersonCreationEvent(
    startTime: Long,
    fingerprintCaptureIds: List<String>,
    sessionId: String = UUID.randomUUID().toString() //StopShip: to change in PAS-993
) : Event(
    UUID.randomUUID().toString(),
    DEFAULT_EVENT_VERSION,
    listOf(EventLabel.SessionId(sessionId)),
    PersonCreationPayload(startTime, DEFAULT_EVENT_VERSION, fingerprintCaptureIds)) {


    // At the end of the sequence of capture, we build a Person object used either for enrolment or verification/identification
    @Keep
    class PersonCreationPayload(
        creationTime: Long,
        version: Int,
        val fingerprintCaptureIds: List<String>
    ) : EventPayload(EventPayloadType.PERSON_CREATION, version, creationTime)

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
            sessionEvents.getEvents()
                .filter {
                    if (it.payload is FingerprintCaptureEvent.FingerprintCapturePayload) {
                        val payload = it.payload
                        payload.fingerprint?.template in personTemplates && payload.result != FingerprintCaptureEvent.FingerprintCapturePayload.Result.SKIPPED
                    } else {
                        false
                    }
                }
                .map { it.id }
    }
}
