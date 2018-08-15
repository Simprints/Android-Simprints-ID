package com.simprints.id.data.analytics.events

import android.support.test.InstrumentationRegistry
import android.support.test.filters.SmallTest
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.id.Application
import com.simprints.id.activities.checkLogin.openedByIntent.CheckLoginFromIntentActivity
import com.simprints.id.data.analytics.events.models.*
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.local.realm.models.rl_Person
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.db.remote.models.fb_Person
import com.simprints.id.di.AppModuleForAndroidTests
import com.simprints.id.di.DaggerForAndroidTests
import com.simprints.id.shared.DependencyRule
import com.simprints.id.shared.anyNotNull
import com.simprints.id.shared.mock
import com.simprints.id.shared.whenever
import com.simprints.id.testSnippets.*
import com.simprints.id.testTools.CalloutCredentials
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.delegates.lazyVar
import com.simprints.id.tools.utils.PeopleGeneratorUtils
import com.simprints.mockscanner.MockBluetoothAdapter
import com.simprints.mockscanner.MockFinger
import com.simprints.mockscanner.MockScannerManager
import io.reactivex.Single
import io.reactivex.rxkotlin.subscribeBy
import io.realm.Realm
import junit.framework.Assert.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import retrofit2.Response
import retrofit2.adapter.rxjava2.Result
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@SmallTest
class SessionEventsManagerImplTest : DaggerForAndroidTests() {

    private val calloutCredentials = CalloutCredentials(
        "bWOFHInKA2YaQwrxZ7uJ",
        "the_one_and_only_module",
        "the_lone_user",
        "d95bacc0-7acb-4ff0-98b3-ae6ecbf7398f")
    private val projectSecret = "Z8nRspDoiQg1QpnDdKE6U7fQKa0GjpQOwnJ4OcSFWulAcIk4+LP9wrtDn8fRmqacLvkmtmOLl+Kxo1emXLsZ0Q=="

    @Rule
    @JvmField
    val loginTestRule = ActivityTestRule(CheckLoginFromIntentActivity::class.java, false, false)

    @Inject lateinit var realmSessionEventsManager: SessionEventsLocalDbManager
    @Inject lateinit var sessionEventsManager: SessionEventsManager
    @Inject lateinit var remoteDbManager: RemoteDbManager
    @Inject lateinit var localDbManager: LocalDbManager
    @Inject lateinit var timeHelper: TimeHelper

    override var module by lazyVar {
        AppModuleForAndroidTests(
            app,
            localDbManagerRule = DependencyRule.SpyRule(),
            bluetoothComponentAdapterRule = DependencyRule.ReplaceRule { mockBluetoothAdapter }
        )
    }

    private lateinit var mockBluetoothAdapter: MockBluetoothAdapter

    @Before
    override fun setUp() {
        app = InstrumentationRegistry.getTargetContext().applicationContext as Application
        super.setUp()

        testAppComponent.inject(this)

        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
        app.initDependencies()

        realmSessionEventsManager.deleteSessions().blockingAwait()
        signOut()
    }

    @After
    fun tearDown() {
    }

    @Test
    fun createSession_shouldReturnASession() {
        val projectId = "some_project"
        sessionEventsManager.createSession(projectId).test().also {
            it.awaitTerminalEvent()
            it.assertComplete()
            it.values().first().also {
                assertNotNull(it)
                assertNotNull(it.id)
                assertNotSame(it.startTime, 0L)
                assertEquals(it.relativeEndTime, 0L)
            }
        }
    }

    @Test
    fun sync_shouldClosePendingSessions() {
        val projectId = "some_project"
        createFakeSession(timeHelper, projectId = projectId, id = "closed_session").apply {
            startTime = timeHelper.msSinceBoot() - 1000
            relativeEndTime = nowRelativeToStartTime(timeHelper) - 10
        }.also { saveSessionInDb(it) }

        createFakeSession(timeHelper, projectId = projectId, id = "still_valid_open_session").apply {
            startTime = timeHelper.msSinceBoot() - 1000
            relativeEndTime = 0
        }.also { saveSessionInDb(it) }

        createFakeSession(timeHelper, projectId = projectId, id = "open_session_but_old").apply {
            startTime = timeHelper.msSinceBoot() - SessionEvents.GRACE_PERIOD - 1000
            relativeEndTime = 0
        }.also { saveSessionInDb(it) }

        realmSessionEventsManager.loadSessions(projectId).blockingGet().also {
            assertEquals(it.size, 3)
        }

        val sessionManagerImpl = (sessionEventsManager as SessionEventsManagerImpl)
        sessionManagerImpl.apiClient = mock()
        whenever(sessionManagerImpl.apiClient.uploadSessions(anyNotNull(), anyNotNull())).thenReturn(Single.just(Result.response(Response.success(Unit))))

        sessionEventsManager.syncSessions(projectId).test().also {
            it.awaitTerminalEvent()
            it.assertComplete()
            val sessions = realmSessionEventsManager.loadSessions().blockingGet()
            assertEquals(sessions.size, 1)
            assertEquals(sessions[0].id, "still_valid_open_session")
        }
    }

    private fun saveSessionInDb(session: SessionEvents) {
        realmSessionEventsManager.insertOrUpdateSessionEvents(session).blockingAwait()
    }

    @Test
    fun createSession_shouldStopPreviousSessions() {
        val projectId = "some_project"
        val oldSession = createFakeSession(projectId = projectId, id = "oldSession")
        assertEquals(oldSession.relativeEndTime, 0)
        realmSessionEventsManager.insertOrUpdateSessionEvents(oldSession).blockingAwait()

        sessionEventsManager.createSession(projectId).test().awaitTerminalEvent()

        val sessions = realmSessionEventsManager.loadSessions(projectId).blockingGet()

        sessions[0].also {
            assertEquals(it.relativeEndTime, 0L)
        }

        sessions[1].also {
            assertEquals(it.id, oldSession.id)
            assertNotSame(it.relativeEndTime, 0L)
            val finalEvent = it.events.find { it is ArtificialTerminationEvent } as ArtificialTerminationEvent?
            assertEquals(finalEvent?.reason, ArtificialTerminationEvent.Reason.NEW_SESSION)
        }
    }

    @Test
    fun userRefusesConsent_sessionShouldNotHaveTheLocation() {

        mockBluetoothAdapter = MockBluetoothAdapter(MockScannerManager(mockFingers = arrayOf()))

        launchActivityEnrol(calloutCredentials, loginTestRule)
        enterCredentialsDirectly(calloutCredentials, projectSecret)
        pressSignIn()
        setupActivityAndDecline()
        Thread.sleep(100)

        sessionEventsManager.getCurrentSession(calloutCredentials.projectId).subscribeBy(
            onSuccess = {
                assertNull(it.location)
            }, onError = { it.printStackTrace() })
    }

    @Test
    fun userAcceptsConsent_sessionShouldHaveTheLocation() {

        mockBluetoothAdapter = MockBluetoothAdapter(MockScannerManager(mockFingers = arrayOf()))

        launchActivityEnrol(calloutCredentials, loginTestRule)
        enterCredentialsDirectly(calloutCredentials, projectSecret)
        pressSignIn()
        setupActivityAndContinue()
        Thread.sleep(100)

        sessionEventsManager.getCurrentSession(calloutCredentials.projectId).subscribeBy(
            onSuccess = {
                assertNotNull(it.location)
            }, onError = { it.printStackTrace() })
    }

    @Test
    fun anErrorWithEvents_shouldBeSwallowed() {
        realmSessionEventsManager.deleteSessions().blockingAwait()

        // There is not activeSession open or pending in the db. So it should fail, but it swallows the error
        val test = sessionEventsManager.updateSession({
            it.location = null
        }).test()
        test.awaitTerminalEvent()
        test.assertNoErrors()
    }

    @Test
    fun enrol_shouldGenerateTheRightEvents() {

        mockBluetoothAdapter = MockBluetoothAdapter(MockScannerManager(mockFingers = arrayOf(
            *MockFinger.person1TwoFingersGoodScan,
            *MockFinger.person1TwoFingersAgainGoodScan,
            *MockFinger.person1TwoFingersGoodScan)))

        // Launch and sign in
        launchActivityEnrol(calloutCredentials, loginTestRule)
        enterCredentialsDirectly(calloutCredentials, projectSecret)
        pressSignIn()

        // Once signed in proceed to enrol person1
        fullHappyWorkflow()
        collectFingerprintsEnrolmentCheckFinished(loginTestRule)

        sessionEventsManager.getCurrentSession(calloutCredentials.projectId).test().also {
            it.awaitTerminalEvent()
            it.assertNoErrors()

            val session = it.values().first()
            assertThat(session.events.map { it.javaClass }).containsExactlyElementsIn(arrayListOf(
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
    }

    @Test
    fun verify_shouldGenerateTheRightEvents() {
        val guid = "123e4567-e89b-12d3-a456-426655440000"

        mockLocalToAddFakePersonAfterLogin(guid)

        mockBluetoothAdapter = MockBluetoothAdapter(MockScannerManager(mockFingers = arrayOf(
            *MockFinger.person1TwoFingersGoodScan,
            *MockFinger.person1TwoFingersAgainGoodScan,
            *MockFinger.person1TwoFingersGoodScan)))

        launchActivityVerify(calloutCredentials, loginTestRule, guid)
        enterCredentialsDirectly(calloutCredentials, projectSecret)
        pressSignIn()

        fullHappyWorkflow()
        matchingActivityVerificationCheckFinished(loginTestRule)

        sessionEventsManager.getCurrentSession(calloutCredentials.projectId).test().also {
            it.awaitTerminalEvent()
            it.assertNoErrors()

            val session = it.values().first()
            assertThat(session.events.map { it.javaClass }).containsExactlyElementsIn(arrayListOf(
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
    }

    @Test
    fun identify_shouldGenerateTheRightEvents() {
        val guid = "123e4567-e89b-12d3-a456-426655440000"
        mockLocalToAddFakePersonAfterLogin(guid)

        mockBluetoothAdapter = MockBluetoothAdapter(MockScannerManager(mockFingers = arrayOf(
            *MockFinger.person1TwoFingersGoodScan,
            *MockFinger.person1TwoFingersAgainGoodScan,
            *MockFinger.person1TwoFingersGoodScan)))

        launchActivityIdentify(calloutCredentials, loginTestRule)
        enterCredentialsDirectly(calloutCredentials, projectSecret)
        pressSignIn()

        fullHappyWorkflow()
        matchingActivityIdentificationCheckFinished(loginTestRule)

        sessionEventsManager.getCurrentSession(calloutCredentials.projectId).test().also {
            it.awaitTerminalEvent()
            it.assertNoErrors()

            val session = it.values().first()
            assertThat(session.events.map { it.javaClass }).containsExactlyElementsIn(arrayListOf(
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
    }

    private fun mockLocalToAddFakePersonAfterLogin(guid: String) {
        Mockito.doAnswer {
            it.callRealMethod()
            localDbManager.insertOrUpdatePersonInLocal(rl_Person(fb_Person(PeopleGeneratorUtils.getRandomPerson(patientId = guid))))
                .onErrorComplete().blockingAwait()
        }.`when`(localDbManager).signInToLocal(anyNotNull())
    }

    private fun createFakeSession(timeHelper: TimeHelper? = null, projectId: String = "some_project", id: String = "some_id"): SessionEvents =
        SessionEvents(
            id = id,
            projectId = projectId,
            appVersionName = "some_version",
            libVersionName = "some_version",
            language = "en",
            device = Device(),
            startTime = timeHelper?.msSinceBoot() ?: 0)

    private fun signOut() {
        remoteDbManager.signOutOfRemoteDb()
    }
}
