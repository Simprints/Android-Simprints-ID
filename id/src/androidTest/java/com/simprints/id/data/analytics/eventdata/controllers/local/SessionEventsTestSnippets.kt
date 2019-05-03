package com.simprints.id.data.analytics.eventdata.controllers.local

import com.google.common.truth.Truth
import com.simprints.id.commontesttools.sessionEvents.createFakeClosedSession
import com.simprints.id.commontesttools.sessionEvents.createFakeSession
import com.simprints.id.data.analytics.eventdata.models.domain.events.*
import com.simprints.id.data.analytics.eventdata.models.domain.session.SessionEvents
import com.simprints.id.data.analytics.eventdata.models.local.*
import com.simprints.id.tools.TimeHelper
import io.realm.Realm
import junit.framework.TestCase.*
import java.util.*

fun verifyEventsForFailedSignedIdFollowedBySucceedSignIn(events: List<Event>) {

    events.filterIsInstance(AuthorizationEvent::class.java).let {
        assertEquals(it.first().result, AuthorizationEvent.Result.NOT_AUTHORIZED)
        assertTrue(it.first().userInfo?.userId.isNullOrEmpty())
        assertTrue(it.first().userInfo?.projectId.isNullOrEmpty())

        assertEquals(it[1].result, AuthorizationEvent.Result.AUTHORIZED)
        assertFalse(it[1].userInfo?.userId.isNullOrEmpty())
        assertFalse(it[1].userInfo?.projectId.isNullOrEmpty())
    }

    events.filterIsInstance(AuthenticationEvent::class.java).let { list ->
        assertEquals(list.first().result, AuthenticationEvent.Result.BAD_CREDENTIALS)
        assertEquals(list[1].result, AuthenticationEvent.Result.AUTHENTICATED)
        list.forEach {
            assertTrue(it.userInfo.userId.isNotEmpty())
            assertTrue(it.userInfo.projectId.isNotEmpty())
        }
    }
}

// TODO : Fix ColloutEvent, CallbackEvent for Session
fun verifyEventsWhenSimprintsIsLaunched(events: List<Event>) {
    val expectedEvents = arrayListOf(
        AuthorizationEvent::class.java,
        ConnectivitySnapshotEvent::class.java
//        CalloutEvent::class.java
    ).map { it.canonicalName }

    Truth.assertThat(events.map { it.javaClass.canonicalName }).containsExactlyElementsIn(expectedEvents)
}

// TODO : Fix ColloutEvent, CallbackEvent for Session
fun verifyEventsAfterEnrolment(events: List<Event>, realmForDataEvent: Realm) {
    val expectedEvents = arrayListOf(
        AuthorizationEvent::class.java,
        AuthenticationEvent::class.java,
        ConnectivitySnapshotEvent::class.java,
//        CalloutEvent::class.java,
        AuthorizationEvent::class.java,
        ScannerConnectionEvent::class.java,
        ConsentEvent::class.java,
        FingerprintCaptureEvent::class.java,
        FingerprintCaptureEvent::class.java,
        PersonCreationEvent::class.java,
        EnrolmentEvent::class.java
//        CallbackEvent::class.java
    ).map { it.canonicalName }

    Truth.assertThat(events.map { it.javaClass.canonicalName }).containsExactlyElementsIn(expectedEvents)
    checkDbHasOnlyTheExpectedInfo(realmForDataEvent, expectedEvents.size)
}

// TODO : Fix ColloutEvent, CallbackEvent for Session
fun verifyEventsAfterVerification(events: List<Event>, realmForDataEvent: Realm) {
    val expectedEvents = arrayListOf(
        AuthorizationEvent::class.java,
        AuthenticationEvent::class.java,
        ConnectivitySnapshotEvent::class.java,
//        CalloutEvent::class.java,
        AuthorizationEvent::class.java,
        ScannerConnectionEvent::class.java,
        CandidateReadEvent::class.java,
        ConsentEvent::class.java,
        FingerprintCaptureEvent::class.java,
        FingerprintCaptureEvent::class.java,
        PersonCreationEvent::class.java,
        OneToOneMatchEvent::class.java
//        CallbackEvent::class.java
    ).map { it.canonicalName }

    Truth.assertThat(events.map { it.javaClass.canonicalName }).containsExactlyElementsIn(expectedEvents)
    checkDbHasOnlyTheExpectedInfo(realmForDataEvent, expectedEvents.size)
}

// TODO : Fix ColloutEvent, CallbackEvent for Session
fun verifyEventsAfterIdentification(events: List<Event>, realmForDataEvent: Realm) {
    val expectedEvents = arrayListOf(
        AuthorizationEvent::class.java,
        AuthenticationEvent::class.java,
        ConnectivitySnapshotEvent::class.java,
//        CalloutEvent::class.java,
        AuthorizationEvent::class.java,
        ScannerConnectionEvent::class.java,
        ConsentEvent::class.java,
        FingerprintCaptureEvent::class.java,
        FingerprintCaptureEvent::class.java,
        PersonCreationEvent::class.java,
        OneToManyMatchEvent::class.java
//        CallbackEvent::class.java
    ).map { it.canonicalName }

    Truth.assertThat(events.map { it.javaClass.canonicalName }).containsExactlyElementsIn(expectedEvents)
    checkDbHasOnlyTheExpectedInfo(realmForDataEvent, expectedEvents.size)
}

fun checkDbHasOnlyTheExpectedInfo(realmForDataEvent: Realm, nEvents: Int) {
    with(realmForDataEvent) {
        realmForDataEvent.executeTransaction {
            assertEquals(nEvents, where(DbEvent::class.java).findAll().size)
            assertEquals(1, where(DbDatabaseInfo::class.java).findAll().size)
            assertEquals(1, where(DbDevice::class.java).findAll().size)
            Truth.assertThat(where(DbLocation::class.java).findAll().size).isIn(arrayListOf(0, 1))
        }
    }
}

fun verifySessionIsOpen(sessionEvents: SessionEvents) {
    assertNotNull(sessionEvents)
    assertNotNull(sessionEvents.id)
    assertNotSame(sessionEvents.startTime, 0L)
    assertEquals(sessionEvents.relativeEndTime, 0L)
}

fun createAndSaveCloseFakeSession(timeHelper: TimeHelper,
                                  realmSessionEventsManager: SessionEventsLocalDbManager,
                                  projectId: String,
                                  id: String = UUID.randomUUID().toString() + "close"): String =
    createFakeClosedSession(timeHelper, projectId, id).also { saveSessionInDb(it, realmSessionEventsManager) }.id

fun createAndSaveOpenFakeSession(timeHelper: TimeHelper,
                                 realmSessionEventsManager: SessionEventsLocalDbManager,
                                 projectId: String,
                                 id: String = UUID.randomUUID().toString() + "open") =
    createFakeSession(timeHelper, projectId, id, timeHelper.nowMinus(1000)).also { saveSessionInDb(it, realmSessionEventsManager) }.id

fun saveSessionInDb(session: SessionEvents, realmSessionEventsManager: SessionEventsLocalDbManager) {
    realmSessionEventsManager.insertOrUpdateSessionEvents(session).blockingAwait()
}

fun verifyNumberOfSessionsInDb(count: Int, realmForDataEvent: Realm) {
    with(realmForDataEvent) {
        assertEquals(count, where(DbSession::class.java).findAll().size)
    }
}

fun verifyNumberOfDatabaseInfosInDb(count: Int, realmForDataEvent: Realm) {
    with(realmForDataEvent) {
        assertEquals(count, where(DbDatabaseInfo::class.java).findAll().size)
    }
}

fun verifyNumberOfEventsInDb(count: Int, realmForDataEvent: Realm) {
    with(realmForDataEvent) {
        assertEquals(count, where(DbEvent::class.java).findAll().size)
    }
}

fun verifyNumberOfDeviceInfosInDb(count: Int, realmForDataEvent: Realm) {
    with(realmForDataEvent) {
        assertEquals(count, where(DbDevice::class.java).findAll().size)
    }
}

fun verifyNumberOfLocationsInDb(count: Int, realmForDataEvent: Realm) {
    with(realmForDataEvent) {
        assertEquals(count, where(DbLocation::class.java).findAll().size)
    }
}
