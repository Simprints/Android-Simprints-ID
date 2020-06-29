package com.simprints.id.data.db.session.domain.models.events

import androidx.annotation.Keep
import com.simprints.core.tools.EncodingUtils
import com.simprints.id.data.db.session.domain.models.session.SessionEvents
import com.simprints.id.data.db.subject.domain.FaceSample
import com.simprints.id.data.db.subject.domain.FingerprintSample
import com.simprints.id.tools.TimeHelper

/*
 * At the end of the sequence of capture, we build a Person object
 * used either for enrolment or verification/identification
 */
@Keep
class PersonCreationEvent(
    startTime: Long,
    val fingerprintCaptureIds: List<String>?,
    val faceCaptureIds: List<String>?
) : Event(EventType.PERSON_CREATION, startTime) {

    companion object {
        fun build(
            timeHelper: TimeHelper,
            currentSession: SessionEvents,
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

        // It extracts CaptureEvents Ids with the templates used to create the "Person" object for
        // identification, verification, enrolment.
        private fun extractFingerprintCaptureEventIdsBasedOnPersonTemplate(
            sessionEvents: SessionEvents,
            personTemplates: List<String>?
        ): List<String>? = sessionEvents.getEvents()
            .filterIsInstance(FingerprintCaptureEvent::class.java)
            .filter {
                personTemplates?.contains(it.fingerprint?.template) ?: false
                    && it.result != FingerprintCaptureEvent.Result.SKIPPED
            }.map { it.id }

        private fun extractFaceCaptureEventIdsBasedOnPersonTemplate(
            sessionEvents: SessionEvents,
            personTemplates: List<String>?
        ): List<String>? = sessionEvents.getEvents()
            .filterIsInstance(FaceCaptureEvent::class.java)
            .filter {
                personTemplates?.contains(it.face?.template) ?: false
            }.map { it.id }

    }
}
