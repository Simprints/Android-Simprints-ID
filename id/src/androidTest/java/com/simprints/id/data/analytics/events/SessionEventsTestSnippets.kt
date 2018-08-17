package com.simprints.id.data.analytics.events

import com.google.common.truth.Truth
import com.simprints.id.data.analytics.events.models.*
import junit.framework.Assert.assertNotSame
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull

fun verifyEventsAfterEnrolment(events: List<Event>) {
    Truth.assertThat(events.map { it.javaClass }).containsExactlyElementsIn(arrayListOf(
        CalloutEvent::class.java,
        ConnectivitySnapshotEvent::class.java,
        AuthorizationEvent::class.java,
        ScannerConnectionEvent::class.java,
        ConsentEvent::class.java,
        FingerprintCaptureEvent::class.java,
        FingerprintCaptureEvent::class.java,
        PersonCreationEvent::class.java,
        EnrollmentEvent::class.java,
        CallbackEvent::class.java
    )).inOrder()
}

fun verifyEventsAfterVerification(events: List<Event>) {
    Truth.assertThat(events.map { it.javaClass }).containsExactlyElementsIn(arrayListOf(
        CalloutEvent::class.java,
        ConnectivitySnapshotEvent::class.java,
        AuthorizationEvent::class.java,
        ScannerConnectionEvent::class.java,
        CandidateReadEvent::class.java,
        ConsentEvent::class.java,
        FingerprintCaptureEvent::class.java,
        FingerprintCaptureEvent::class.java,
        PersonCreationEvent::class.java,
        OneToOneMatchEvent::class.java,
        CallbackEvent::class.java
    )).inOrder()
}

fun verifyEventsAfterIdentification(events: List<Event>) {
    Truth.assertThat(events.map { it.javaClass }).containsExactlyElementsIn(arrayListOf(
        CalloutEvent::class.java,
        ConnectivitySnapshotEvent::class.java,
        AuthorizationEvent::class.java,
        ScannerConnectionEvent::class.java,
        ConsentEvent::class.java,
        FingerprintCaptureEvent::class.java,
        FingerprintCaptureEvent::class.java,
        PersonCreationEvent::class.java,
        OneToManyMatchEvent::class.java,
        CallbackEvent::class.java
    )).inOrder()
}

fun verifySessionIsOpen(sessionEvents: SessionEvents){
    assertNotNull(sessionEvents)
    assertNotNull(sessionEvents.id)
    assertNotSame(sessionEvents.startTime, 0L)
    assertEquals(sessionEvents.relativeEndTime, 0L)
}
