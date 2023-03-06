package com.simprints.infra.events.sampledata

import com.simprints.infra.events.event.domain.models.Event
import com.simprints.infra.events.event.domain.models.EventLabels
import java.util.*

object SessionBuilder {

    fun buildEnrolmentSession(
        labels: EventLabels = EventLabels(
            sessionId = UUID.randomUUID().toString(),
            projectId = SampleDefaults.DEFAULT_PROJECT_ID,
            deviceId = SampleDefaults.DEFAULT_DEVICE_ID
        ),
        isClosed: Boolean = false
    ): List<Event> {
        return listOf(
            AuthorizationEventSample.getEvent(labels, isClosed),
            CompletionCheckEventSample.getEvent(labels, isClosed),
            ConnectivitySnapshotEventSample.getEvent(labels, isClosed),
            EnrolmentCallbackEventSample.getEvent(labels, isClosed),
            EnrolmentCalloutEventSample.getEvent(labels, isClosed),
            IntentParsingEventSample.getEvent(labels, isClosed),
            PersonCreationEventSample.getEvent(labels, isClosed),
            ScannerConnectionEventSample.getEvent(labels, isClosed),
            SessionCaptureEventSample.getEvent(labels, isClosed),
            Vero2InfoSnapshotEventSample.getEvent(labels, isClosed)
        )
    }

}
