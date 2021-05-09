package com.simprints.id.sampledata

import com.simprints.id.data.db.event.domain.models.Event
import java.util.*

object SessionBuilder {

    fun buildEnrolmentSession(
        sessionId: String = UUID.randomUUID().toString(),
        subjectId: String = UUID.randomUUID().toString(),
        isClosed: Boolean = false
    ): List<Event> {
        return listOf(
            AuthorizationEventSample.getEvent(sessionId, subjectId, isClosed),
            CompletionCheckEventSample.getEvent(sessionId, subjectId, isClosed),
            ConnectivitySnapshotEventSample.getEvent(sessionId, subjectId, isClosed),
            EnrolmentCallbackEventSample.getEvent(sessionId, subjectId, isClosed),
            EnrolmentCalloutEventSample.getEvent(sessionId, subjectId, isClosed),
            FingerprintCaptureEventSample.getEvent(sessionId, subjectId, isClosed),
            IntentParsingEventSample.getEvent(sessionId, subjectId, isClosed),
            PersonCreationEventSample.getEvent(sessionId, subjectId, isClosed),
            ScannerConnectionEventSample.getEvent(sessionId, subjectId, isClosed),
            SessionCaptureEventSample.getEvent(sessionId, subjectId, isClosed),
            Vero2InfoSnapshotEventSample.getEvent(sessionId, subjectId, isClosed)
        )
    }

}
