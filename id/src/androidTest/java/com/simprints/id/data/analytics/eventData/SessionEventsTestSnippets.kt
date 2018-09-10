package com.simprints.id.data.analytics.eventData

import com.google.common.truth.Truth
import com.simprints.id.data.analytics.eventData.models.events.*
import com.simprints.id.data.analytics.eventData.models.session.DatabaseInfo
import com.simprints.id.data.analytics.eventData.models.session.Device
import com.simprints.id.data.analytics.eventData.models.session.Location
import com.simprints.id.data.analytics.eventData.models.session.SessionEvents
import com.simprints.id.data.analytics.eventData.realm.RlEvent
import io.realm.Realm
import junit.framework.Assert.assertNotSame
import org.junit.Assert.*

fun verifyEventsForFailedSignedIdFollowedBySucceedSignIn(events: List<Event>) {

    events.filterIsInstance(AuthorizationEvent::class.java).let {
        assertEquals(it.first().result, AuthorizationEvent.Result.NOT_AUTHORIZED)
        assertTrue(it.first().userInfo?.userId.isNullOrEmpty())
        assertTrue(it.first().userInfo?.projectId.isNullOrEmpty())

        assertEquals(it[1].result, AuthorizationEvent.Result.AUTHORIZED)
        assertFalse(it[1].userInfo?.userId.isNullOrEmpty())
        assertFalse(it[1].userInfo?.projectId.isNullOrEmpty())
    }

    events.filterIsInstance(AuthenticationEvent::class.java).let {
        assertEquals(it.first().result, AuthenticationEvent.Result.BAD_CREDENTIALS)
        assertEquals(it[1].result, AuthenticationEvent.Result.AUTHENTICATED)
        it.forEach {
            assertTrue(it.userInfo.userId.isNotEmpty())
            assertTrue(it.userInfo.projectId.isNotEmpty())
        }
    }
}

fun verifyEventsAfterEnrolment(events: List<Event>, realmForDataEvent: Realm) {
    val expectedEvents = arrayListOf(
        AuthorizationEvent::class.java,
        AuthenticationEvent::class.java,
        ConnectivitySnapshotEvent::class.java,
        CalloutEvent::class.java,
        AuthorizationEvent::class.java,
        ScannerConnectionEvent::class.java,
        ConsentEvent::class.java,
        FingerprintCaptureEvent::class.java,
        FingerprintCaptureEvent::class.java,
        PersonCreationEvent::class.java,
        EnrollmentEvent::class.java,
        CallbackEvent::class.java
    )
    Truth.assertThat(events.map { it.javaClass }).containsExactlyElementsIn(expectedEvents)
    checkDbHasOnlyTheExpectedInfo(realmForDataEvent, expectedEvents.size)
}

fun verifyEventsAfterVerification(events: List<Event>, realmForDataEvent: Realm) {
    val expectedEvents = arrayListOf(
        AuthorizationEvent::class.java,
        AuthenticationEvent::class.java,
        ConnectivitySnapshotEvent::class.java,
        CalloutEvent::class.java,
        AuthorizationEvent::class.java,
        ScannerConnectionEvent::class.java,
        CandidateReadEvent::class.java,
        ConsentEvent::class.java,
        FingerprintCaptureEvent::class.java,
        FingerprintCaptureEvent::class.java,
        PersonCreationEvent::class.java,
        OneToOneMatchEvent::class.java,
        CallbackEvent::class.java
    )

    Truth.assertThat(events.map { it.javaClass }).containsExactlyElementsIn(expectedEvents)
    checkDbHasOnlyTheExpectedInfo(realmForDataEvent, expectedEvents.size)
}

fun verifyEventsAfterIdentification(events: List<Event>, realmForDataEvent: Realm) {
    val expectedEvents = arrayListOf(
        AuthorizationEvent::class.java,
        AuthenticationEvent::class.java,
        ConnectivitySnapshotEvent::class.java,
        CalloutEvent::class.java,
        AuthorizationEvent::class.java,
        ScannerConnectionEvent::class.java,
        ConsentEvent::class.java,
        FingerprintCaptureEvent::class.java,
        FingerprintCaptureEvent::class.java,
        PersonCreationEvent::class.java,
        OneToManyMatchEvent::class.java,
        CallbackEvent::class.java
    )

    Truth.assertThat(events.map { it.javaClass }).containsExactlyElementsIn(expectedEvents)
    checkDbHasOnlyTheExpectedInfo(realmForDataEvent, expectedEvents.size)
}

fun checkDbHasOnlyTheExpectedInfo(realmForDataEvent: Realm, nEvents: Int) {
    with(realmForDataEvent) {
        realmForDataEvent.executeTransaction {
            assertEquals(nEvents, where(RlEvent::class.java).findAll().size)
            assertEquals(1, where(DatabaseInfo::class.java).findAll().size)
            assertEquals(1, where(Device::class.java).findAll().size)
            Truth.assertThat(where(Location::class.java).findAll().size).isIn(arrayListOf(0, 1))
        }
    }
}

fun verifySessionIsOpen(sessionEvents: SessionEvents) {
    assertNotNull(sessionEvents)
    assertNotNull(sessionEvents.id)
    assertNotSame(sessionEvents.startTime, 0L)
    assertEquals(sessionEvents.relativeEndTime, 0L)
}
