package com.simprints.id.data.analytics.eventData

import android.support.test.InstrumentationRegistry
import android.support.test.filters.SmallTest
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.google.common.truth.Truth
import com.nhaarman.mockito_kotlin.argumentCaptor
import com.simprints.id.Application
import com.simprints.id.activities.checkLogin.openedByIntent.CheckLoginFromIntentActivity
import com.simprints.id.data.analytics.eventData.models.events.ArtificialTerminationEvent
import com.simprints.id.data.analytics.eventData.models.events.FingerprintCaptureEvent
import com.simprints.id.data.analytics.eventData.models.events.PersonCreationEvent
import com.simprints.id.data.analytics.eventData.models.session.DatabaseInfo
import com.simprints.id.data.analytics.eventData.models.session.Device
import com.simprints.id.data.analytics.eventData.models.session.Location
import com.simprints.id.data.analytics.eventData.models.session.SessionEvents
import com.simprints.id.data.analytics.eventData.realm.RealmSessionEventsDbManagerImpl
import com.simprints.id.data.analytics.eventData.realm.RlEvent
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.local.realm.models.rl_Person
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.db.remote.models.fb_Person
import com.simprints.id.data.db.remote.sessions.RemoteSessionsManager
import com.simprints.id.data.prefs.settings.SettingsPreferencesManager
import com.simprints.id.di.AppModuleForAndroidTests
import com.simprints.id.di.DaggerForAndroidTests
import com.simprints.id.shared.*
import com.simprints.id.testSnippets.*
import com.simprints.id.testTools.CalloutCredentials
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.delegates.lazyVar
import com.simprints.id.shared.PeopleGeneratorUtils
import com.simprints.id.tools.json.JsonHelper
import com.simprints.libcommon.Person
import com.simprints.libcommon.Utils
import com.simprints.libsimprints.FingerIdentifier
import com.simprints.mockscanner.MockBluetoothAdapter
import com.simprints.mockscanner.MockFinger
import com.simprints.mockscanner.MockScannerManager
import io.reactivex.Single
import io.reactivex.rxkotlin.subscribeBy
import io.realm.Realm
import junit.framework.Assert
import junit.framework.Assert.*
import okhttp3.Protocol
import okhttp3.Request
import org.json.JSONObject
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

    private val testProjectId = "test_project"
    private val calloutCredentials = CalloutCredentials(
        "bWOFHInKA2YaQwrxZ7uJ",
        "the_one_and_only_module",
        "the_lone_user",
        "d95bacc0-7acb-4ff0-98b3-ae6ecbf7398f")
    private val projectSecret = "Z8nRspDoiQg1QpnDdKE6U7fQKa0GjpQOwnJ4OcSFWulAcIk4+LP9wrtDn8fRmqacLvkmtmOLl+Kxo1emXLsZ0Q=="

    @Rule
    @JvmField
    val simprintsActionTestRule = ActivityTestRule(CheckLoginFromIntentActivity::class.java, false, false)

    @Inject lateinit var realmSessionEventsManager: SessionEventsLocalDbManager
    @Inject lateinit var sessionEventsManagerSpy: SessionEventsManager
    @Inject lateinit var settingsPreferencesManagerSpy: SettingsPreferencesManager
    @Inject lateinit var remoteDbManager: RemoteDbManager
    @Inject lateinit var remoteSessionsManager: RemoteSessionsManager
    @Inject lateinit var localDbManager: LocalDbManager
    @Inject lateinit var timeHelper: TimeHelper

    override var preferencesModule: PreferencesModuleForAnyTests by lazyVar {
        PreferencesModuleForAnyTests(settingsPreferencesManagerRule = DependencyRule.SpyRule)
    }

    override var module by lazyVar {
        AppModuleForAndroidTests(
            app,
            localDbManagerRule = DependencyRule.SpyRule,
            remoteDbManagerRule = DependencyRule.SpyRule,
            remoteSessionsManagerRule = DependencyRule.SpyRule,
            sessionEventsManagerRule = DependencyRule.SpyRule,
            bluetoothComponentAdapterRule = DependencyRule.ReplaceRule { mockBluetoothAdapter }
        )
    }

    private lateinit var mockBluetoothAdapter: MockBluetoothAdapter
    private val realmForDataEvent
        get() = (realmSessionEventsManager as RealmSessionEventsDbManagerImpl).getRealmInstance().blockingGet()

    @Before
    override fun setUp() {
        app = InstrumentationRegistry.getTargetContext().applicationContext as Application
        super.setUp()

        testAppComponent.inject(this)

        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
        app.initDependencies()

        realmForDataEvent.executeTransaction {
            it.deleteAll()
        }

        signOut()

        whenever(settingsPreferencesManagerSpy.fingerStatus).thenReturn(hashMapOf(
            FingerIdentifier.LEFT_THUMB to true,
            FingerIdentifier.LEFT_INDEX_FINGER to true))
    }

    @After
    fun tearDown() {
    }

    @Test
    fun createSession_shouldReturnASession() {
        sessionEventsManagerSpy.createSession().test().also {
            it.awaitTerminalEvent()
            it.assertComplete()
            verifySessionIsOpen(it.values().first())
        }
    }

    @Test
    fun serialiseSession_shouldIgnoreSkipSerialisationProperty() {
        sessionEventsManagerSpy.createSession().blockingGet()
        sessionEventsManagerSpy.updateSession({
            it.databaseInfo = DatabaseInfo(0)
            it.location = Location(0.0, 0.0)
        }).blockingGet()

        val session = sessionEventsManagerSpy.getCurrentSession().blockingGet()
        Assert.assertNotNull(session.device.id)
        Assert.assertNotNull(session.databaseInfo?.id)
        Assert.assertNotNull(session.location?.id)

        val jsonString = JsonHelper.toJson(session)
        val jsonObject = JSONObject(jsonString)

        Assert.assertFalse(jsonObject.getJSONObject("device").has("id"))
        Assert.assertFalse(jsonObject.getJSONObject("databaseInfo").has("id"))
        Assert.assertFalse(jsonObject.getJSONObject("location").has("id"))
    }

    @Test
    fun sync_shouldClosePendingSessions() {
        createAndSaveFakeCloseSession()
        val openSessionId = createAndSaveFakeOpenSession()
        createAndSaveFakeExpiredOpenSession()

        val mockSessionsApi = mock<SessionsRemoteInterface>()

        whenever(mockSessionsApi.uploadSessions(
            anyNotNull(),
            anyNotNull())).thenReturn(Single.just(Result.response(buildSuccessfulUploadSessionResponse())))

        whenever(remoteSessionsManager.getSessionsApiClient()).thenReturn(Single.just(mockSessionsApi))

        sessionEventsManagerSpy.syncSessions(testProjectId).test().also {
            it.awaitTerminalEvent()
            it.assertComplete()
            val sessions = realmSessionEventsManager.loadSessions().blockingGet()
            with(realmForDataEvent) {
                assertEquals(0, where(RlEvent::class.java).findAll().size)
                assertEquals(0, where(DatabaseInfo::class.java).findAll().size)
                assertEquals(1, where(Device::class.java).findAll().size)
                assertEquals(0, where(Location::class.java).findAll().size)
            }

            assertEquals(1, sessions.size)
            assertEquals(openSessionId, sessions[0].id)
        }
    }

    private fun buildSuccessfulUploadSessionResponse() =
        Response.success<Void?>(null, okhttp3.Response.Builder() //
            .code(201)
            .message("OK")
            .protocol(Protocol.HTTP_1_1)
            .request(Request.Builder().url("http://localhost/").build())
            .build())

    @Test
    fun createSession_shouldStopPreviousSessions() {
        val oldSession = createFakeSession(projectId = testProjectId, id = "oldSession")
        assertEquals(oldSession.relativeEndTime, 0)
        realmSessionEventsManager.insertOrUpdateSessionEvents(oldSession).blockingAwait()

        sessionEventsManagerSpy.createSession().blockingGet()
        sessionEventsManagerSpy.updateSession({ it.projectId = testProjectId }).blockingGet()

        val sessions = realmSessionEventsManager.loadSessions(testProjectId).blockingGet()

        sessions[0].also {
            assertTrue(it.isOpen())
        }

        sessions[1].also {
            assertEquals(it.id, oldSession.id)
            assertTrue(it.isClosed())
            val finalEvent = it.events.find { it is ArtificialTerminationEvent } as ArtificialTerminationEvent?
            assertEquals(finalEvent?.reason, ArtificialTerminationEvent.Reason.NEW_SESSION)
        }
    }

    @Test
    fun userRefusesConsent_sessionShouldNotHaveTheLocation() {
        mockBluetoothAdapter = MockBluetoothAdapter(MockScannerManager(mockFingers = arrayOf(*MockFinger.person1TwoFingersGoodScan)))

        launchActivityEnrol(calloutCredentials, simprintsActionTestRule)
        enterCredentialsDirectly(calloutCredentials, projectSecret)
        pressSignIn()
        setupActivityAndDecline()
        Thread.sleep(100)

        sessionEventsManagerSpy.getCurrentSession(calloutCredentials.projectId).subscribeBy(
            onSuccess = {
                assertNull(it.location)
            }, onError = { it.printStackTrace() })
    }

    @Test
    fun userAcceptsConsent_sessionShouldHaveTheLocation() {
        mockBluetoothAdapter = MockBluetoothAdapter(MockScannerManager(mockFingers = arrayOf(*MockFinger.person1TwoFingersGoodScan)))

        launchActivityEnrol(calloutCredentials, simprintsActionTestRule)
        enterCredentialsDirectly(calloutCredentials, projectSecret)
        pressSignIn()
        setupActivityAndContinue()
        Thread.sleep(100)

        sessionEventsManagerSpy.getCurrentSession(calloutCredentials.projectId).subscribeBy(
            onSuccess = {
                assertNotNull(it.location)
            }, onError = { it.printStackTrace() })
    }

    @Test
    fun anErrorWithEvents_shouldBeSwallowed() {
        realmSessionEventsManager.deleteSessions().blockingAwait()

        // There is not activeSession open or pending in the db. So it should fail, but it swallows the error
        val test = sessionEventsManagerSpy.updateSession({
            it.location = null
        }).test()
        test.awaitTerminalEvent()
        test.assertNoErrors()
    }

    @Test
    fun enrol_shouldGenerateTheRightEvents() {
        mockBluetoothAdapter = MockBluetoothAdapter(MockScannerManager(mockFingers = arrayOf(*MockFinger.person1TwoFingersGoodScan)))

        // Launch and sign in
        launchActivityEnrol(calloutCredentials, simprintsActionTestRule)
        enterCredentialsDirectly(calloutCredentials, projectSecret)
        pressSignIn()

        // Once signed in proceed to enrol person1
        fullHappyWorkflow()
        collectFingerprintsEnrolmentCheckFinished(simprintsActionTestRule)

        sessionEventsManagerSpy.getCurrentSession(calloutCredentials.projectId).test().also {
            it.awaitTerminalEvent()
            it.assertNoErrors()

            verifyEventsAfterEnrolment(it.values().first().events, realmForDataEvent)
        }
    }

    @Test
    fun login_shouldGenerateTheRightEvents() {
        mockBluetoothAdapter = MockBluetoothAdapter(MockScannerManager(mockFingers = arrayOf(*MockFinger.person1TwoFingersGoodScan)))

        // Launch and sign in
        launchActivityEnrol(calloutCredentials, simprintsActionTestRule)
        enterCredentialsDirectly(calloutCredentials, projectSecret + "wrong")
        pressSignIn()

        Thread.sleep(6000)
        enterCredentialsDirectly(calloutCredentials, projectSecret)
        pressSignIn()
        setupActivityAndContinue()

        sessionEventsManagerSpy.getCurrentSession(calloutCredentials.projectId).test().also {
            it.awaitTerminalEvent()
            it.assertNoErrors()

            verifyEventsForFailedSignedIdFollowedBySucceedSignIn(it.values().first().events)
        }
    }

    @Test
    fun verify_shouldGenerateTheRightEvents() {
        val guid = "123e4567-e89b-12d3-a456-426655440000"
        mockBluetoothAdapter = MockBluetoothAdapter(MockScannerManager(mockFingers = arrayOf(*MockFinger.person1TwoFingersGoodScan)))

        mockLocalToAddFakePersonAfterLogin(guid)

        launchActivityVerify(calloutCredentials, simprintsActionTestRule, guid)
        enterCredentialsDirectly(calloutCredentials, projectSecret)
        pressSignIn()

        fullHappyWorkflow()
        matchingActivityVerificationCheckFinished(simprintsActionTestRule)

        sessionEventsManagerSpy.getCurrentSession(calloutCredentials.projectId).test().also {
            it.awaitTerminalEvent()
            it.assertNoErrors()

            verifyEventsAfterVerification(it.values().first().events, realmForDataEvent)
        }
    }

    @Test
    fun identify_shouldGenerateTheRightEvents() {
        val guid = "123e4567-e89b-12d3-a456-426655440000"
        mockBluetoothAdapter = MockBluetoothAdapter(MockScannerManager(mockFingers = arrayOf(*MockFinger.person1TwoFingersGoodScan)))

        mockLocalToAddFakePersonAfterLogin(guid)

        launchActivityIdentify(calloutCredentials, simprintsActionTestRule)
        enterCredentialsDirectly(calloutCredentials, projectSecret)
        pressSignIn()

        fullHappyWorkflow()

        matchingActivityIdentificationCheckFinished(simprintsActionTestRule)

        sessionEventsManagerSpy.getCurrentSession(calloutCredentials.projectId).test().also {
            it.awaitTerminalEvent()
            it.assertNoErrors()

            verifyEventsAfterIdentification(it.values().first().events, realmForDataEvent)
        }
    }

    @Test
    fun multipleScans_shouldGenerateACreatePersonEventWithRightTemplates() {
        mockBluetoothAdapter = MockBluetoothAdapter(MockScannerManager(mockFingers = arrayOf(
            MockFinger.PERSON_1_VERSION_1_LEFT_THUMB_BAD_SCAN,
            MockFinger.PERSON_1_VERSION_2_LEFT_INDEX_GOOD_SCAN,
            MockFinger.PERSON_1_VERSION_1_LEFT_THUMB_GOOD_SCAN,
            MockFinger.PERSON_1_VERSION_1_LEFT_INDEX_GOOD_SCAN)))

        launchActivityIdentify(calloutCredentials, simprintsActionTestRule)
        enterCredentialsDirectly(calloutCredentials, projectSecret)
        pressSignIn()

        setupActivityAndContinue()

        collectFingerprintsPressScan()
        skipFinger()
        waitForSplashScreenAppearsAndDisappears().also { Thread.sleep(2000) }

        collectFingerprintsPressScan()
        collectFingerprintsPressScan()

        checkIfDialogIsDisplayedWithResultAndClickConfirm("× LEFT THUMB\n✓ LEFT INDEX FINGER\n✓ RIGHT THUMB\n")

        matchingActivityIdentificationCheckFinished(simprintsActionTestRule)

        val personCreatedArg = argumentCaptor<Person>()
        Mockito.verify(sessionEventsManagerSpy, Mockito.times(1)).addPersonCreationEventInBackground(personCreatedArg.capture())

        sessionEventsManagerSpy.getCurrentSession(calloutCredentials.projectId).test().also {
            it.awaitTerminalEvent()
            it.assertNoErrors()
            val session = it.values().first()

            val personCreatedForMatchingActivity = personCreatedArg.firstValue
            val personCreationEvent = session.events.filterIsInstance(PersonCreationEvent::class.java)[0]
            val usefulTemplatesFromEvents = session.events
                .filterIsInstance(FingerprintCaptureEvent::class.java)
                .filter { it.id in personCreationEvent.fingerprintCaptureIds }
                .map { it.fingerprint?.template }

            Truth.assertThat(usefulTemplatesFromEvents)
                .containsExactlyElementsIn(personCreatedForMatchingActivity.fingerprints.map {
                    Utils.byteArrayToBase64(it.templateBytes)
                })

            val skippedEvent = session.events
                .filterIsInstance(FingerprintCaptureEvent::class.java)
                .findLast { it.result == FingerprintCaptureEvent.Result.SKIPPED }

            assertNotNull(skippedEvent)
        }
    }

    private fun mockLocalToAddFakePersonAfterLogin(guid: String) {
        Mockito.doAnswer {
            it.callRealMethod()
            localDbManager.insertOrUpdatePersonInLocal(rl_Person(fb_Person(PeopleGeneratorUtils.getRandomPerson(patientId = guid))))
                .onErrorComplete().blockingAwait()
        }.`when`(localDbManager).signInToLocal(anyNotNull())
    }

    private fun createAndSaveFakeCloseSession(projectId: String = testProjectId, id: String = "close_session"): String =
        timeHelper.let { it ->
            createFakeSession(it, projectId, id).apply {
                startTime = it.now() - 1000
                relativeEndTime = nowRelativeToStartTime(it) - 10
            }.also { saveSessionInDb(it) }.id
        }

    private fun createAndSaveFakeOpenSession(projectId: String = testProjectId, id: String = "open_session") =
        timeHelper.let {
            createFakeSession(it, projectId, id).apply {
                startTime = it.now() - 1000
                relativeEndTime = 0
            }.also { saveSessionInDb(it) }.id
        }

    private fun createAndSaveFakeExpiredOpenSession(projectId: String = testProjectId, id: String = "open_expired_session") =
        timeHelper.let {
            createFakeSession(it, projectId, id).apply {
                startTime = it.now() - SessionEvents.GRACE_PERIOD - 1000
                relativeEndTime = 0
            }.also { saveSessionInDb(it) }.id
        }

    private fun saveSessionInDb(session: SessionEvents) {
        realmSessionEventsManager.insertOrUpdateSessionEvents(session).blockingAwait()
    }

    private fun createFakeSession(timeHelper: TimeHelper? = null, projectId: String = testProjectId, id: String = "some_id"): SessionEvents =
        SessionEvents(
            id = id,
            projectId = projectId,
            appVersionName = "some_version",
            libVersionName = "some_version",
            language = "en",
            device = Device(),
            startTime = timeHelper?.now() ?: 0)

    private fun signOut() {
        remoteDbManager.signOutOfRemoteDb()
    }
}
