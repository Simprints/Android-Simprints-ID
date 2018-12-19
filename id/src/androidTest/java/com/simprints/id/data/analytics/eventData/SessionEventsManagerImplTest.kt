package com.simprints.id.data.analytics.eventData

import androidx.test.InstrumentationRegistry
import androidx.test.filters.SmallTest
import androidx.test.runner.AndroidJUnit4
import com.google.common.truth.Truth
import com.nhaarman.mockito_kotlin.argumentCaptor
import com.simprints.id.Application
import com.simprints.id.data.analytics.eventData.controllers.domain.SessionEventsManager
import com.simprints.id.data.analytics.eventData.controllers.local.RealmSessionEventsDbManagerImpl
import com.simprints.id.data.analytics.eventData.controllers.local.SessionEventsLocalDbManager
import com.simprints.id.data.analytics.eventData.controllers.remote.apiAdapters.SessionEventsApiAdapterFactory
import com.simprints.id.data.analytics.eventData.models.domain.events.ArtificialTerminationEvent
import com.simprints.id.data.analytics.eventData.models.domain.events.FingerprintCaptureEvent
import com.simprints.id.data.analytics.eventData.models.domain.events.PersonCreationEvent
import com.simprints.id.data.analytics.eventData.models.domain.session.SessionEvents
import com.simprints.id.data.analytics.eventData.models.local.RlSession
import com.simprints.id.data.analytics.eventData.models.local.toDomainSession
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.local.realm.models.toRealmPerson
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.prefs.settings.SettingsPreferencesManager
import com.simprints.id.di.AppModuleForAndroidTests
import com.simprints.id.di.DaggerForAndroidTests
import com.simprints.id.shared.*
import com.simprints.id.shared.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.shared.DefaultTestConstants.DEFAULT_PROJECT_SECRET
import com.simprints.id.shared.DefaultTestConstants.DEFAULT_REALM_KEY
import com.simprints.id.shared.DefaultTestConstants.DEFAULT_TEST_CALLOUT_CREDENTIALS
import com.simprints.id.shared.sessionEvents.createFakeSession
import com.simprints.id.testSnippets.*
import com.simprints.id.testTemplates.FirstUseLocal
import com.simprints.id.testTools.ActivityUtils
import com.simprints.id.testTools.waitOnUi
import com.simprints.id.tools.RandomGenerator
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.delegates.lazyVar
import com.simprints.libcommon.Person
import com.simprints.libcommon.Utils
import com.simprints.libsimprints.FingerIdentifier
import com.simprints.mockscanner.MockBluetoothAdapter
import com.simprints.mockscanner.MockFinger
import com.simprints.mockscanner.MockScannerManager
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.Sort
import junit.framework.Assert.*
import org.json.JSONObject
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import java.util.*
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@SmallTest
class SessionEventsManagerImplTest : DaggerForAndroidTests(), FirstUseLocal {

    override var peopleRealmConfiguration: RealmConfiguration? = null
    override var sessionsRealmConfiguration: RealmConfiguration? = null

    @Rule @JvmField val simprintsActionTestRule = ActivityUtils.checkLoginFromIntentActivityTestRule()

    @Inject lateinit var randomGeneratorMock: RandomGenerator
    @Inject lateinit var realmSessionEventsManager: SessionEventsLocalDbManager
    @Inject lateinit var sessionEventsManagerSpy: SessionEventsManager
    @Inject lateinit var settingsPreferencesManagerSpy: SettingsPreferencesManager
    @Inject lateinit var remoteDbManager: RemoteDbManager
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
            sessionEventsManagerRule = DependencyRule.SpyRule,
            scheduledSessionsSyncManagerRule = DependencyRule.MockRule,
            randomGeneratorRule = DependencyRule.MockRule,
            bluetoothComponentAdapterRule = DependencyRule.ReplaceRule { mockBluetoothAdapter }
        )
    }

    private lateinit var mockBluetoothAdapter: MockBluetoothAdapter
    private val realmForDataEvent
        get() = (realmSessionEventsManager as RealmSessionEventsDbManagerImpl).getRealmInstance().blockingGet()

    private val mostRecentSessionInDb: SessionEvents
        get() {
            realmForDataEvent.refresh()
            return realmForDataEvent
                .where(RlSession::class.java)
                .findAll()
                .sort("startTime", Sort.DESCENDING).first()!!.toDomainSession()
        }

    @Before
    override fun setUp() {
        app = InstrumentationRegistry.getTargetContext().applicationContext as Application
        super<DaggerForAndroidTests>.setUp()
        testAppComponent.inject(this)

        setupRandomGeneratorToGenerateKey(DEFAULT_REALM_KEY, randomGeneratorMock)

        app.initDependencies()

        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
        peopleRealmConfiguration = FirstUseLocal.defaultPeopleRealmConfiguration
        sessionsRealmConfiguration = FirstUseLocal.defaultSessionRealmConfiguration
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
        val result = sessionEventsManagerSpy.createSession().test()

        result.waitForCompletionAndAssertNoErrors()
        val newSession = result.values().first()
        verifySessionIsOpen(newSession)
    }

    @Test
    fun sessionCount_shouldBeAccurate() {
        mockBluetoothAdapter = MockBluetoothAdapter(MockScannerManager(mockFingers = arrayOf(*MockFinger.person1TwoFingersGoodScan)))
        val numberOfPreviousSessions = 5
        repeat(numberOfPreviousSessions) { createAndSaveCloseSession(projectId = DEFAULT_PROJECT_ID, id = UUID.randomUUID().toString()) }

        launchActivityEnrol(DEFAULT_TEST_CALLOUT_CREDENTIALS, simprintsActionTestRule)
        enterCredentialsDirectly(DEFAULT_TEST_CALLOUT_CREDENTIALS, DEFAULT_PROJECT_SECRET)
        pressSignIn()
        setupActivityAndContinue()
        waitOnUi(100)

        realmForDataEvent.refresh()
        with(mostRecentSessionInDb) {
            val jsonString = SessionEventsApiAdapterFactory().gson.toJson(this)
            val jsonObject = JSONObject(jsonString)
            assertTrue(jsonObject.getJSONObject("databaseInfo").has("sessionCount"))
            assertEquals(numberOfPreviousSessions + 1, jsonObject.getJSONObject("databaseInfo").getInt("sessionCount"))
        }
    }

    @Test
    fun createSession_shouldStopPreviousSessions() {
        val oldSession = createFakeSession(projectId = DEFAULT_PROJECT_ID, id = "oldSession")
            .also { saveSessionInDb(it, realmSessionEventsManager) }

        sessionEventsManagerSpy.createSession().blockingGet()
        sessionEventsManagerSpy.updateSession { it.projectId = DEFAULT_PROJECT_ID }.blockingGet()

        val sessions = realmSessionEventsManager.loadSessions(DEFAULT_PROJECT_ID).blockingGet()
        val oldSessionFromDb = sessions[0]
        oldSessionFromDb.also {
            assertTrue(it.isOpen())
        }
        val newSessionFromDb = sessions[1]
        newSessionFromDb.also {
            assertEquals(it.id, oldSession.id)
            assertTrue(it.isClosed())
            val finalEvent = it.events.filterIsInstance(ArtificialTerminationEvent::class.java).first()
            assertEquals(finalEvent.reason, ArtificialTerminationEvent.Reason.NEW_SESSION)
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

        assertNull(mostRecentSessionInDb.location)
    }

    @Test
    fun userAcceptsConsent_sessionShouldHaveTheLocation() {
        mockBluetoothAdapter = MockBluetoothAdapter(MockScannerManager(mockFingers = arrayOf(*MockFinger.person1TwoFingersGoodScan)))

        launchActivityEnrol(DEFAULT_TEST_CALLOUT_CREDENTIALS, simprintsActionTestRule)
        enterCredentialsDirectly(DEFAULT_TEST_CALLOUT_CREDENTIALS, DEFAULT_PROJECT_SECRET)
        pressSignIn()
        setupActivityAndContinue()
        Thread.sleep(100)

        assertNotNull(mostRecentSessionInDb.location)
    }

    @Test
    fun anErrorWithEvents_shouldBeSwallowed() {
        realmSessionEventsManager.deleteSessions()

        // There is not activeSession open or pending in the db. So it should fail, but it swallows the error
        sessionEventsManagerSpy.updateSession {
            it.location = null
        }.test().waitForCompletionAndAssertNoErrors()
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

        realmForDataEvent.refresh()
        verifyEventsAfterEnrolment(mostRecentSessionInDb.events, realmForDataEvent)
    }

    @Test
    fun login_shouldGenerateTheRightEvents() {
        mockBluetoothAdapter = MockBluetoothAdapter(MockScannerManager(mockFingers = arrayOf(*MockFinger.person1TwoFingersGoodScan)))

        // Launch and sign in
        launchActivityEnrol(DEFAULT_TEST_CALLOUT_CREDENTIALS, simprintsActionTestRule)
        enterCredentialsDirectly(DEFAULT_TEST_CALLOUT_CREDENTIALS, DEFAULT_PROJECT_SECRET + "wrong")
        pressSignIn()
        Thread.sleep(6000)
        enterCredentialsDirectly(DEFAULT_TEST_CALLOUT_CREDENTIALS, DEFAULT_PROJECT_SECRET)
        pressSignIn()
        setupActivityAndContinue()

        verifyEventsForFailedSignedIdFollowedBySucceedSignIn(mostRecentSessionInDb.events)
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

        verifyEventsAfterVerification(mostRecentSessionInDb.events, realmForDataEvent)
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

        verifyEventsAfterIdentification(mostRecentSessionInDb.events, realmForDataEvent)
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

        val eventsInMostRecentSession = mostRecentSessionInDb.events
        val personCreatedForMatchingActivity = personCreatedArg.firstValue
        val personCreationEvent = eventsInMostRecentSession.filterIsInstance(PersonCreationEvent::class.java)[0]
        val usefulTemplatesFromEvents = eventsInMostRecentSession
            .filterIsInstance(FingerprintCaptureEvent::class.java)
            .filter { it.id in personCreationEvent.fingerprintCaptureIds }
            .map { it.fingerprint?.template }

        Truth.assertThat(usefulTemplatesFromEvents)
            .containsExactlyElementsIn(personCreatedForMatchingActivity.fingerprints.map {
                Utils.byteArrayToBase64(it.templateBytes)
            })

        val skippedFingerCaptureEvent = eventsInMostRecentSession
            .filterIsInstance(FingerprintCaptureEvent::class.java)
            .findLast { it.result == FingerprintCaptureEvent.Result.SKIPPED }

        assertNotNull(skippedFingerCaptureEvent)
    }

    private fun mockLocalToAddFakePersonAfterLogin(guid: String) {
        Mockito.doAnswer {
            it.callRealMethod()
            localDbManager.insertOrUpdatePersonInLocal(PeopleGeneratorUtils.getRandomPerson(patientId = guid).toRealmPerson())
                .onErrorComplete().blockingAwait()
        }.`when`(localDbManager).signInToLocal(anyNotNull())
    }

    private fun createAndSaveCloseSession(projectId: String = DEFAULT_PROJECT_ID, id: String) =
        createAndSaveCloseFakeSession(timeHelper, realmSessionEventsManager, projectId, id)

    private fun signOut() {
        remoteDbManager.signOutOfRemoteDb()
    }
}
