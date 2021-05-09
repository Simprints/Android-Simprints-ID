package com.simprints.id.sampledata

import com.simprints.id.data.db.event.domain.models.EventLabels
import com.simprints.id.data.db.event.domain.models.fingerprint.FingerprintCaptureEvent
import com.simprints.id.data.db.event.domain.models.fingerprint.FingerprintTemplateFormat
import com.simprints.id.data.db.subject.domain.FingerIdentifier
import com.simprints.id.sampledata.DEFAULTS.CREATED_AT
import com.simprints.id.sampledata.DEFAULTS.ENDED_AT
import com.simprints.id.sampledata.DEFAULTS.GUID1

object FingerprintCaptureEventSample : SampleEvent() {

    override fun getEvent(
        sessionId: String,
        subjectId: String,
        isClosed: Boolean
    ): FingerprintCaptureEvent {
        val labels = EventLabels(sessionId = GUID1)
        val fingerprint = FingerprintCaptureEvent.FingerprintCapturePayload.Fingerprint(
            FingerIdentifier.LEFT_THUMB,
            8,
            "template",
            FingerprintTemplateFormat.ISO_19794_2
        )

        return FingerprintCaptureEvent(
            CREATED_AT,
            ENDED_AT,
            FingerIdentifier.LEFT_THUMB,
            10,
            FingerprintCaptureEvent.FingerprintCapturePayload.Result.BAD_QUALITY,
            fingerprint,
            GUID1,
            labels
        )
    }

}
