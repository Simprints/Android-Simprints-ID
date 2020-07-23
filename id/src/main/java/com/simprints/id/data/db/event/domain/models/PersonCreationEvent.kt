package com.simprints.id.data.db.event.domain.models

import androidx.annotation.Keep
import com.simprints.core.tools.EncodingUtils
import com.simprints.id.data.db.event.domain.models.EventLabel.SessionIdLabel
import com.simprints.id.data.db.event.domain.models.EventType.PERSON_CREATION
import com.simprints.id.data.db.event.domain.models.session.SessionCaptureEvent
import com.simprints.id.data.db.subject.domain.FingerprintSample
import com.simprints.id.tools.TimeHelper
import java.util.*

@Keep
class PersonCreationEvent(
    override val id: String = UUID.randomUUID().toString(),
    override val labels: MutableList<EventLabel>,
    override val payload: PersonCreationPayload,
    override val type: EventType
) : Event(id, labels, payload, type) {

    constructor(
        startTime: Long,
        fingerprintCaptureIds: List<String>,
        sessionId: String = UUID.randomUUID().toString() //StopShip: to change in PAS-993
    ) : this(
        UUID.randomUUID().toString(),
        mutableListOf(SessionIdLabel(sessionId)),
        PersonCreationPayload(startTime, EVENT_VERSION, fingerprintCaptureIds),
        PERSON_CREATION)


    // At the end of the sequence of capture, we build a Person object used either for enrolment or verification/identification
    @Keep
    class PersonCreationPayload(
        override val createdAt: Long,
        override val eventVersion: Int,
        val fingerprintCaptureIds: List<String>
    ) : EventPayload(PERSON_CREATION, eventVersion, createdAt)

    companion object {
        fun build(timeHelper: TimeHelper,
                  currentSession: SessionCaptureEvent,
                  fingerprintSamples: List<FingerprintSample>) =
            PersonCreationEvent(
                timeHelper.now(),
                extractCaptureEventIdsBasedOnPersonTemplate(currentSession, fingerprintSamples.map { EncodingUtils.byteArrayToBase64(it.template) })
            )

        // It extracts CaptureEvents Ids with the templates used to create the "Person" object for
        // identification, verification, enrolment.
        private fun extractCaptureEventIdsBasedOnPersonTemplate(sessionEvent: SessionCaptureEvent, personTemplates: List<String>): List<String> =
            emptyList() //StopShip
//            sessionEvent.getEvents()
//                .filter {
//                    if (it.payload is FingerprintCaptureEvent.FingerprintCapturePayload) {
//                        val payload = it.payload
//                        payload.fingerprint?.template in personTemplates && payload.result != FingerprintCaptureEvent.FingerprintCapturePayload.Result.SKIPPED
//                    } else {
//                        false
//                    }
//                }
//                .map { it.id }

        const val EVENT_VERSION = DEFAULT_EVENT_VERSION

    }
}
