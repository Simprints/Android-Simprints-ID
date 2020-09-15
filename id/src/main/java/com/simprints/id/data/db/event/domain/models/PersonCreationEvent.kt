package com.simprints.id.data.db.event.domain.models

import androidx.annotation.Keep
import com.simprints.core.tools.EncodingUtils
import com.simprints.id.data.db.event.domain.models.EventType.PERSON_CREATION
import com.simprints.id.data.db.event.domain.models.session.SessionCaptureEvent
import com.simprints.id.data.db.event.local.models.DbEvent.Companion.DEFAULT_EVENT_VERSION
import com.simprints.id.data.db.subject.domain.FaceSample
import com.simprints.id.data.db.subject.domain.FingerprintSample
import com.simprints.id.tools.time.TimeHelper
import java.util.*

@Keep
data class PersonCreationEvent(
    override val id: String = UUID.randomUUID().toString(),
    override var labels: EventLabels,
    override val payload: PersonCreationPayload,
    override val type: EventType
) : Event() {

    constructor(
        startTime: Long,
        fingerprintCaptureIds: List<String>,
        faceCaptureIds: List<String>,
        labels: EventLabels = EventLabels() //StopShip: to change in PAS-993
    ) : this(
        UUID.randomUUID().toString(),
        labels,
        PersonCreationPayload(startTime, EVENT_VERSION, fingerprintCaptureIds, faceCaptureIds),
        PERSON_CREATION)


    // At the end of the sequence of capture, we build a Person object used either for enrolment or verification/identification
    @Keep
    data class PersonCreationPayload(
        override val createdAt: Long,
        override val eventVersion: Int,
        val fingerprintCaptureIds: List<String>,
        val faceCaptureIds: List<String>,
        override val type: EventType = PERSON_CREATION,
        override val endedAt: Long = 0
    ) : EventPayload()

    companion object {
        fun build(
            timeHelper: TimeHelper,
            currentSession: SessionCaptureEvent,
            fingerprintSamples: List<FingerprintSample>?,
            faceSamples: List<FaceSample>?
        ) = PersonCreationEvent(
            startTime = timeHelper.now(),
            fingerprintCaptureIds = extractFingerprintCaptureEventIdsBasedOnPersonTemplate(
                currentSession,
                fingerprintSamples?.map { EncodingUtils.byteArrayToBase64(it.template) }
            ),
            faceCaptureIds = extractFaceCaptureEventIdsBasedOnPersonTemplate(
                currentSession,
                faceSamples?.map { EncodingUtils.byteArrayToBase64(it.template) }
            )
        )

        //STOPSHIP
        // It extracts CaptureEvents Ids with the templates used to create the "Person" object for
        // identification, verification, enrolment.
        private fun extractFingerprintCaptureEventIdsBasedOnPersonTemplate(
            sessionEvents: SessionCaptureEvent,
            personTemplates: List<String>?
        ): List<String> = emptyList()
//            sessionEvents.getEvents()
//            .filterIsInstance(FingerprintCaptureEvent::class.java)
//            .filter {
//                personTemplates?.contains(it.fingerprint?.template) ?: false
//                    && it.result != FingerprintCaptureEvent.Result.SKIPPED
//            }.map { it.id }

        private fun extractFaceCaptureEventIdsBasedOnPersonTemplate(
            sessionEvents: SessionCaptureEvent,
            personTemplates: List<String>?
        ): List<String> = emptyList()
//            sessionEvents.getEvents()
//            .filterIsInstance(FaceCaptureEvent::class.java)
//            .filter {
//                personTemplates?.contains(it.face?.template) ?: false
//            }.map { it.id }

        const val EVENT_VERSION = 1

    }
}
