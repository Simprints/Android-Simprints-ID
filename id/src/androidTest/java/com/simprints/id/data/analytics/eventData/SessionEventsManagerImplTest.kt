package com.simprints.id.data.analytics.eventData

import android.support.test.InstrumentationRegistry
import android.support.test.filters.SmallTest
import android.support.test.runner.AndroidJUnit4
import com.google.common.truth.Truth
import com.nhaarman.mockito_kotlin.argumentCaptor
import com.simprints.id.Application
import com.simprints.id.data.analytics.eventData.models.events.ArtificialTerminationEvent
import com.simprints.id.data.analytics.eventData.models.events.FingerprintCaptureEvent
import com.simprints.id.data.analytics.eventData.models.events.GuidSelectionEvent
import com.simprints.id.data.analytics.eventData.models.events.PersonCreationEvent
import com.simprints.id.data.analytics.eventData.models.session.DatabaseInfo
import com.simprints.id.data.analytics.eventData.models.session.Device
import com.simprints.id.data.analytics.eventData.models.session.Location
import com.simprints.id.data.analytics.eventData.models.session.SessionEvents
import com.simprints.id.data.analytics.eventData.realm.RealmSessionEventsDbManagerImpl
import com.simprints.id.data.analytics.eventData.realm.RlEvent
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.local.realm.PeopleRealmConfig
import com.simprints.id.data.db.local.realm.models.rl_Person
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.db.remote.models.fb_Person
import com.simprints.id.data.prefs.settings.SettingsPreferencesManager
import com.simprints.id.di.AppModuleForAndroidTests
import com.simprints.id.di.DaggerForAndroidTests
import com.simprints.id.shared.*
import com.simprints.id.testSnippets.*
import com.simprints.id.testTemplates.FirstUseLocal
import com.simprints.id.testTools.*
import com.simprints.id.shared.DefaultTestConstants.DEFAULT_LOCAL_DB_KEY
import com.simprints.id.shared.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.shared.DefaultTestConstants.DEFAULT_PROJECT_SECRET
import com.simprints.id.shared.DefaultTestConstants.DEFAULT_REALM_KEY
import com.simprints.id.shared.DefaultTestConstants.DEFAULT_TEST_CALLOUT_CREDENTIALS
import com.simprints.id.tools.RandomGenerator
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.delegates.lazyVar
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
import io.realm.RealmConfiguration
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
import java.util.*
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@SmallTest
class SessionEventsManagerImplTest : DaggerForAndroidTests(), FirstUseLocal {

    override var peopleRealmConfiguration: RealmConfiguration? = null
    
    @Rule @JvmField val simprintsActionTestRule = ActivityUtils.checkLoginFromIntentActivityTestRule()

    @Inject lateinit var randomGeneratorMock: RandomGenerator
    @Inject lateinit var realmSessionEventsManager: SessionEventsLocalDbManager
    @Inject lateinit var sessionEventsManagerSpy: SessionEventsManager
    @Inject lateinit var settingsPreferencesManagerSpy: SettingsPreferencesManager
    @Inject lateinit var remoteDbManager: RemoteDbManager
    @Inject lateinit var localDbManager: LocalDbManager
    @Inject lateinit var timeHelper: TimeHelper
    
    private lateinit var mockBluetoothAdapter: MockBluetoothAdapter
    private val realmForDataEvent
        get() = (realmSessionEventsManager as RealmSessionEventsDbManagerImpl).getRealmInstance().blockingGet()

    override var preferencesModule: PreferencesModuleForAnyTests by lazyVar {
        PreferencesModuleForAnyTests(settingsPreferencesManagerRule = DependencyRule.SpyRule)
    }

    override var module by lazyVar {
        AppModuleForAndroidTests(
            app,
            localDbManagerRule = DependencyRule.SpyRule,
            remoteDbManagerRule = DependencyRule.SpyRule,
            sessionEventsManagerRule = DependencyRule.SpyRule,
            randomGeneratorRule = DependencyRule.MockRule,
            bluetoothComponentAdapterRule = DependencyRule.ReplaceRule { mockBluetoothAdapter }
        )
    }

    @Before
    override fun setUp() {
        app = InstrumentationRegistry.getTargetContext().applicationContext as Application
        super<DaggerForAndroidTests>.setUp()
        testAppComponent.inject(this)

        setupRandomGeneratorToGenerateKey(DEFAULT_REALM_KEY, randomGeneratorMock)

        app.initDependencies()

        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
        peopleRealmConfiguration = PeopleRealmConfig.get(DEFAULT_LOCAL_DB_KEY.projectId, DEFAULT_LOCAL_DB_KEY.value, DEFAULT_LOCAL_DB_KEY.projectId)
        super<FirstUseLocal>.setUp()

        signOut()

        whenever(settingsPreferencesManagerSpy.fingerStatus).thenReturn(hashMapOf(
            FingerIdentifier.LEFT_THUMB to true,
            FingerIdentifier.LEFT_INDEX_FINGER to true))
    }

    @After
    override fun tearDown() {
        super.tearDown()
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

            it.events.add(GuidSelectionEvent(200, "some_guid"))
            it.events.add(FingerprintCaptureEvent(100, 2000, FingerIdentifier.LEFT_INDEX_FINGER, 60, FingerprintCaptureEvent.Result.GOOD_SCAN, null))
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
        Assert.assertFalse(jsonObject.has("projectId"))

        // In general, events should not have an id or an eventId property once serialised
        val guidSelectionEventJson = jsonObject.getJSONArray("events").getJSONObject(0)
        Assert.assertFalse(guidSelectionEventJson.has("eventId"))
        Assert.assertFalse(guidSelectionEventJson.has("id"))

        // FingerprintCaptureEvent is the only event that should have an id field
        val fingerprintCaptureEvent = jsonObject.getJSONArray("events").getJSONObject(1)
        Assert.assertFalse(fingerprintCaptureEvent.has("eventId"))
        Assert.assertTrue(fingerprintCaptureEvent.has("id"))
    }

    @Test
    fun sessionCount_shouldBeAccurate() {
        mockBluetoothAdapter = MockBluetoothAdapter(MockScannerManager(mockFingers = arrayOf(*MockFinger.person1TwoFingersGoodScan)))

        val numberOfPreviousSessions = 5

        repeat(numberOfPreviousSessions) { createAndSaveFakeCloseSession(projectId = DEFAULT_PROJECT_ID, id = UUID.randomUUID().toString()) }

        launchActivityEnrol(DEFAULT_TEST_CALLOUT_CREDENTIALS, simprintsActionTestRule)
        enterCredentialsDirectly(DEFAULT_TEST_CALLOUT_CREDENTIALS, DEFAULT_PROJECT_SECRET)
        pressSignIn()
        setupActivityAndContinue()
        waitOnUi(100)

        val session = sessionEventsManagerSpy.getCurrentSession().blockingGet()

        val jsonString = JsonHelper.toJson(session)
        val jsonObject = JSONObject(jsonString)

        Assert.assertTrue(jsonObject.getJSONObject("databaseInfo").has("sessionCount"))
        assertEquals(numberOfPreviousSessions + 1, jsonObject.getJSONObject("databaseInfo").getInt("sessionCount"))
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

        whenever(remoteDbManager.getSessionsApiClient()).thenReturn(Single.just(mockSessionsApi))

        sessionEventsManagerSpy.syncSessions(DEFAULT_PROJECT_ID).test().also {
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
        val oldSession = createFakeSession(projectId = DEFAULT_PROJECT_ID, id = "oldSession")
        assertEquals(oldSession.relativeEndTime, 0)
        realmSessionEventsManager.insertOrUpdateSessionEvents(oldSession).blockingAwait()

        sessionEventsManagerSpy.createSession().blockingGet()
        sessionEventsManagerSpy.updateSession({ it.projectId = DEFAULT_PROJECT_ID }).blockingGet()

        val sessions = realmSessionEventsManager.loadSessions(DEFAULT_PROJECT_ID).blockingGet()

        sessions[0].also {
            assertTrue(it.isOpen())
        }

        sessions[1].also { sessionEvents ->
            assertEquals(sessionEvents.id, oldSession.id)
            assertTrue(sessionEvents.isClosed())
            val finalEvent = sessionEvents.events.find { it is ArtificialTerminationEvent } as ArtificialTerminationEvent?
            assertEquals(finalEvent?.reason, ArtificialTerminationEvent.Reason.NEW_SESSION)
        }
    }

    @Test
    fun userRefusesConsent_sessionShouldNotHaveTheLocation() {
        mockBluetoothAdapter = MockBluetoothAdapter(MockScannerManager(mockFingers = arrayOf(*MockFinger.person1TwoFingersGoodScan)))

        launchActivityEnrol(DEFAULT_TEST_CALLOUT_CREDENTIALS, simprintsActionTestRule)
        enterCredentialsDirectly(DEFAULT_TEST_CALLOUT_CREDENTIALS, DEFAULT_PROJECT_SECRET)
        pressSignIn()
        setupActivityAndDecline()
        Thread.sleep(100)

        sessionEventsManagerSpy.getCurrentSession(DEFAULT_TEST_CALLOUT_CREDENTIALS.projectId).subscribeBy(
            onSuccess = {
                assertNull(it.location)
            }, onError = { it.printStackTrace() })
    }

    @Test
    fun userAcceptsConsent_sessionShouldHaveTheLocation() {
        mockBluetoothAdapter = MockBluetoothAdapter(MockScannerManager(mockFingers = arrayOf(*MockFinger.person1TwoFingersGoodScan)))

        launchActivityEnrol(DEFAULT_TEST_CALLOUT_CREDENTIALS, simprintsActionTestRule)
        enterCredentialsDirectly(DEFAULT_TEST_CALLOUT_CREDENTIALS, DEFAULT_PROJECT_SECRET)
        pressSignIn()
        setupActivityAndContinue()
        Thread.sleep(100)

        sessionEventsManagerSpy.getCurrentSession(DEFAULT_TEST_CALLOUT_CREDENTIALS.projectId).subscribeBy(
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
        launchActivityEnrol(DEFAULT_TEST_CALLOUT_CREDENTIALS, simprintsActionTestRule)
        enterCredentialsDirectly(DEFAULT_TEST_CALLOUT_CREDENTIALS, DEFAULT_PROJECT_SECRET)
        pressSignIn()

        // Once signed in proceed to enrol person1
        fullHappyWorkflow()
        collectFingerprintsEnrolmentCheckFinished(simprintsActionTestRule)

        sessionEventsManagerSpy.getCurrentSession(DEFAULT_TEST_CALLOUT_CREDENTIALS.projectId).test().also {
            it.awaitTerminalEvent()
            it.assertNoErrors()

            verifyEventsAfterEnrolment(it.values().first().events, realmForDataEvent)
        }
    }

    @Test
    fun login_shouldGenerateTheRightEvents() {
        mockBluetoothAdapter = MockBluetoothAdapter(MockScannerManager(mockFingers = arrayOf(*MockFinger.person1TwoFingersGoodScan)))

        // Launch and sign in
        launchActivityEnrol(DEFAULT_TEST_CALLOUT_CREDENTIALS, simprintsActionTestRule)
        enterCredentialsDirectly(DEFAULT_TEST_CALLOUT_CREDENTIALS, DEFAULT_PROJECT_SECRET + "wrong")
        pressSignIn()

        waitOnUi(8000)
        enterCredentialsDirectly(DEFAULT_TEST_CALLOUT_CREDENTIALS, DEFAULT_PROJECT_SECRET)
        pressSignIn()
        setupActivityAndContinue()

        sessionEventsManagerSpy.getCurrentSession(DEFAULT_TEST_CALLOUT_CREDENTIALS.projectId).test().also {
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

        launchActivityVerify(DEFAULT_TEST_CALLOUT_CREDENTIALS, simprintsActionTestRule, guid)
        enterCredentialsDirectly(DEFAULT_TEST_CALLOUT_CREDENTIALS, DEFAULT_PROJECT_SECRET)
        pressSignIn()

        fullHappyWorkflow()
        matchingActivityVerificationCheckFinished(simprintsActionTestRule)

        sessionEventsManagerSpy.getCurrentSession(DEFAULT_TEST_CALLOUT_CREDENTIALS.projectId).test().also {
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

        launchActivityIdentify(DEFAULT_TEST_CALLOUT_CREDENTIALS, simprintsActionTestRule)
        enterCredentialsDirectly(DEFAULT_TEST_CALLOUT_CREDENTIALS, DEFAULT_PROJECT_SECRET)
        pressSignIn()

        fullHappyWorkflow()

        matchingActivityIdentificationCheckFinished(simprintsActionTestRule)

        sessionEventsManagerSpy.getCurrentSession(DEFAULT_TEST_CALLOUT_CREDENTIALS.projectId).test().also {
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

        launchActivityIdentify(DEFAULT_TEST_CALLOUT_CREDENTIALS, simprintsActionTestRule)
        enterCredentialsDirectly(DEFAULT_TEST_CALLOUT_CREDENTIALS, DEFAULT_PROJECT_SECRET)
        pressSignIn()

        setupActivityAndContinue()

        collectFingerprintsPressScan()
        skipFinger()
        waitForSplashScreenAppearsAndDisappears()

        collectFingerprintsPressScan()
        collectFingerprintsPressScan()

        checkIfDialogIsDisplayedWithResultAndClickConfirm("× LEFT THUMB\n✓ LEFT INDEX FINGER\n✓ RIGHT THUMB\n")

        matchingActivityIdentificationCheckFinished(simprintsActionTestRule)

        val personCreatedArg = argumentCaptor<Person>()
        Mockito.verify(sessionEventsManagerSpy, Mockito.times(1)).addPersonCreationEventInBackground(personCreatedArg.capture())

        sessionEventsManagerSpy.getCurrentSession(DEFAULT_TEST_CALLOUT_CREDENTIALS.projectId).test().also { testObserver ->
            testObserver.awaitTerminalEvent()
            testObserver.assertNoErrors()
            val session = testObserver.values().first()

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

    private fun createAndSaveFakeCloseSession(projectId: String = DEFAULT_PROJECT_ID, id: String = "close_session"): String =
        timeHelper.let { it ->
            createFakeSession(it, projectId, id).apply {
                startTime = it.now() - 1000
                relativeEndTime = nowRelativeToStartTime(it) - 10
            }.also { saveSessionInDb(it) }.id
        }

    private fun createAndSaveFakeOpenSession(projectId: String = DEFAULT_PROJECT_ID, id: String = "open_session") =
        timeHelper.let { timeHelper ->
            createFakeSession(timeHelper, projectId, id).apply {
                startTime = timeHelper.now() - 1000
                relativeEndTime = 0
            }.also { saveSessionInDb(it) }.id
        }

    private fun createAndSaveFakeExpiredOpenSession(projectId: String = DEFAULT_PROJECT_ID, id: String = "open_expired_session") =
        timeHelper.let { timeHelper ->
            createFakeSession(timeHelper, projectId, id).apply {
                startTime = timeHelper.now() - SessionEvents.GRACE_PERIOD - 1000
                relativeEndTime = 0
            }.also { saveSessionInDb(it) }.id
        }

    private fun saveSessionInDb(session: SessionEvents) {
        realmSessionEventsManager.insertOrUpdateSessionEvents(session).blockingAwait()
    }

    private fun createFakeSession(timeHelper: TimeHelper? = null, projectId: String = DEFAULT_PROJECT_ID, id: String = "some_id"): SessionEvents =
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
