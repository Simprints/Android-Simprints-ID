package com.simprints.id.data.db.event.domain.models

import androidx.annotation.Keep
import com.simprints.core.tools.EncodingUtils
import com.simprints.id.data.db.event.domain.models.EventType.PERSON_CREATION
import com.simprints.id.data.db.event.domain.models.face.FaceCaptureEvent
import com.simprints.id.data.db.subject.domain.FaceSample
import com.simprints.id.data.db.subject.domain.FingerprintSample
import com.simprints.id.data.db.subject.domain.uniqueId
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
        fingerprintReferenceId: String?,
        faceCaptureIds: List<String>,
        faceReferenceId: String?,
        labels: EventLabels = EventLabels() //StopShip: to change in PAS-993
    ) : this(
        UUID.randomUUID().toString(),
        labels,
        PersonCreationPayload(startTime, EVENT_VERSION, fingerprintCaptureIds, fingerprintReferenceId, faceCaptureIds, faceReferenceId),
        PERSON_CREATION)


    // At the end of the sequence of capture, we build a Person object used either for enrolment, verification or identification
    @Keep
    data class PersonCreationPayload(
        override val createdAt: Long,
        override val eventVersion: Int,
        val fingerprintCaptureIds: List<String>,
        val fingerprintReferenceId: String?,
        val faceCaptureIds: List<String>,
        val faceReferenceId: String?,
        override val type: EventType = PERSON_CREATION,
        override val endedAt: Long = 0
    ) : EventPayload()

    companion object {
        fun build(
            timeHelper: TimeHelper,
            faceCaptureEvents: List<FaceCaptureEvent>,
            fingerprintCaptureEvents: List<FingerprintCaptureEvent>,
            faceSamplesForPersonCreation: List<FaceSample>?,
            fingerprintSamplesForPersonCreation: List<FingerprintSample>?
        ) = PersonCreationEvent(
            startTime = timeHelper.now(),
            fingerprintCaptureIds = extractFingerprintCaptureEventIdsBasedOnPersonTemplate(
                fingerprintCaptureEvents,
                fingerprintSamplesForPersonCreation?.map { EncodingUtils.byteArrayToBase64(it.template) }
            ),
            fingerprintReferenceId = fingerprintSamplesForPersonCreation?.uniqueId(),
            faceCaptureIds = extractFaceCaptureEventIdsBasedOnPersonTemplate(
                faceCaptureEvents,
                faceSamplesForPersonCreation?.map { EncodingUtils.byteArrayToBase64(it.template) }
            ),
            faceReferenceId = faceSamplesForPersonCreation?.uniqueId()
        )

        private fun extractFingerprintCaptureEventIdsBasedOnPersonTemplate(
            captureEvents: List<FingerprintCaptureEvent>,
            personTemplates: List<String>?
        ): List<String> =
            captureEvents
                .filter {
                    personTemplates?.contains(it.payload.fingerprint?.template) ?: false
                        && it.payload.result != FingerprintCaptureEvent.FingerprintCapturePayload.Result.SKIPPED
                }.map { it.id }

        private fun extractFaceCaptureEventIdsBasedOnPersonTemplate(
            captureEvents: List<FaceCaptureEvent>,
            personTemplates: List<String>?
        ): List<String> =
            captureEvents
                .filter {
                    personTemplates?.contains(it.payload.face?.template) ?: false
                }.map { it.id }

        const val EVENT_VERSION = 1
    }
}
