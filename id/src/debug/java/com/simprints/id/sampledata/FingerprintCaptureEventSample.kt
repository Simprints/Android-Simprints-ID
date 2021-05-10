package com.simprints.id.sampledata

import com.simprints.id.data.db.event.domain.models.EventLabels
import com.simprints.id.data.db.event.domain.models.fingerprint.FingerprintCaptureEvent
import com.simprints.id.data.db.event.domain.models.fingerprint.FingerprintTemplateFormat
import com.simprints.id.data.db.subject.domain.FingerIdentifier
import com.simprints.id.sampledata.SampleDefaults.CREATED_AT
import com.simprints.id.sampledata.SampleDefaults.ENDED_AT

object FingerprintCaptureEventSample : SampleEvent() {

    override fun getEvent(
        labels: EventLabels,
        isClosed: Boolean
    ): FingerprintCaptureEvent {
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
            labels = labels
        )
    }

}
