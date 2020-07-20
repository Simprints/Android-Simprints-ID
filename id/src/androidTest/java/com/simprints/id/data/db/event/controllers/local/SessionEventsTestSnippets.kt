package com.simprints.id.data.db.event.controllers.local

import com.google.common.truth.Truth
import com.simprints.id.data.db.event.domain.models.*
import com.simprints.id.data.db.event.domain.models.AuthenticationEvent.AuthenticationPayload
import com.simprints.id.data.db.event.domain.models.AuthenticationEvent.AuthenticationPayload.Result.AUTHENTICATED
import com.simprints.id.data.db.event.domain.models.AuthenticationEvent.AuthenticationPayload.Result.BAD_CREDENTIALS
import com.simprints.id.data.db.event.domain.models.AuthorizationEvent.AuthorizationPayload
import com.simprints.id.data.db.event.domain.models.AuthorizationEvent.AuthorizationPayload.Result.AUTHORIZED
import com.simprints.id.data.db.event.domain.models.AuthorizationEvent.AuthorizationPayload.Result.NOT_AUTHORIZED
import io.realm.Realm
import junit.framework.TestCase.*

fun verifyEventsForFailedSignedIdFollowedBySucceedSignIn(events: List<Event>) {

    events.filterIsInstance(AuthorizationEvent::class.java).let {
        with(it.first().payload as AuthorizationPayload) {
            assertEquals(result, NOT_AUTHORIZED)
            assertTrue(userInfo?.userId.isNullOrEmpty())
            assertTrue(userInfo?.projectId.isNullOrEmpty())
        }

        val secondPayload = it[1].payload as AuthorizationPayload
        assertEquals(secondPayload.result, AUTHORIZED)
        assertFalse(secondPayload.userInfo?.userId.isNullOrEmpty())
        assertFalse(secondPayload.userInfo?.projectId.isNullOrEmpty())
    }

    events.filterIsInstance(AuthenticationEvent::class.java).let { list ->
        assertEquals((list.first().payload as AuthenticationPayload).result, BAD_CREDENTIALS)
        assertEquals((list[1].payload as AuthenticationPayload).result, AUTHENTICATED)
        list.map { it.payload as AuthenticationPayload }.forEach {
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
}


//fun verifySessionIsOpen(sessionEvents: SessionEvents) {
//    assertNotNull(sessionEvents)
//    assertNotNull(sessionEvents.id)
//    assertNotSame(sessionEvents.startTime, 0L)
//    assertEquals(sessionEvents.relativeEndTime, 0L)
//}
//
//fun createAndSaveCloseFakeSession(timeHelper: TimeHelper,
//                                  realmSessionManager: SessionLocalDataSource,
//                                  projectId: String,
//                                  id: String = UUID.randomUUID().toString() + "close"): String =
//    createFakeClosedSession(timeHelper, projectId, id).also { saveSessionInDb(it, realmSessionManager) }.id
//
//fun createAndSaveOpenFakeSession(timeHelper: TimeHelper,
//                                 realmSessionManager: SessionLocalDataSource,
//                                 projectId: String,
//                                 id: String = UUID.randomUUID().toString() + "open") =
//    createFakeSession(timeHelper, projectId, id, timeHelper.nowMinus(1000)).also { saveSessionInDb(it, realmSessionManager) }.id
//
//fun saveSessionInDb(session: SessionEvents, realmSessionManager: SessionLocalDataSource) {
//    realmSessionManager.insertOrUpdateSessionEvents(session).blockingAwait()
//}
//
//fun verifyNumberOfSessionsInDb(count: Int, realmForDataEvent: Realm) {
//    with(realmForDataEvent) {
//        assertEquals(count, where(DbSession::class.java).findAll().size)
//    }
//}
//
//fun verifyNumberOfDatabaseInfosInDb(count: Int, realmForDataEvent: Realm) {
//    with(realmForDataEvent) {
//        assertEquals(count, where(DbDatabaseInfo::class.java).findAll().size)
//    }
//}
//
//fun verifyNumberOfEventsInDb(count: Int, realmForDataEvent: Realm) {
//    with(realmForDataEvent) {
//        assertEquals(count, where(DbEvent::class.java).findAll().size)
//    }
//}
//
//fun verifyNumberOfDeviceInfosInDb(count: Int, realmForDataEvent: Realm) {
//    with(realmForDataEvent) {
//        assertEquals(count, where(DbDevice::class.java).findAll().size)
//    }
//}
//
//fun verifyNumberOfLocationsInDb(count: Int, realmForDataEvent: Realm) {
//    with(realmForDataEvent) {
//        assertEquals(count, where(DbLocation::class.java).findAll().size)
//    }
//}
