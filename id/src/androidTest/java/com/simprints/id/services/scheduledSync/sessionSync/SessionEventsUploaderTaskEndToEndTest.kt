package com.simprints.id.services.scheduledSync.sessionSync

import android.support.test.InstrumentationRegistry
import android.support.test.filters.SmallTest
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.google.common.truth.Truth
import com.simprints.id.Application
import com.simprints.id.activities.checkLogin.openedByIntent.CheckLoginFromIntentActivity
import com.simprints.id.data.analytics.eventData.controllers.domain.SessionEventsManager
import com.simprints.id.data.analytics.eventData.controllers.local.SessionEventsLocalDbManager
import com.simprints.id.data.analytics.eventData.models.domain.session.SessionEvents
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.prefs.settings.SettingsPreferencesManager
import com.simprints.id.di.AppModuleForAndroidTests
import com.simprints.id.di.DaggerForAndroidTests
import com.simprints.id.services.scheduledSync.sessionSync.SessionEventsSyncMasterTask.Companion.BATCH_SIZE
import com.simprints.id.shared.DefaultTestConstants.DEFAULT_REALM_KEY
import com.simprints.id.shared.DependencyRule
import com.simprints.id.shared.PreferencesModuleForAnyTests
import com.simprints.id.shared.sessionEvents.createFakeClosedSession
import com.simprints.id.shared.waitForCompletionAndAssertNoErrors
import com.simprints.id.shared.whenever
import com.simprints.id.testSnippets.*
import com.simprints.id.testTemplates.FirstUseLocalAndRemote
import com.simprints.id.testTools.adapters.toCalloutCredentials
import com.simprints.id.testTools.models.TestProject
import com.simprints.id.testTools.remote.RemoteTestingManager
import com.simprints.id.tools.RandomGenerator
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.delegates.lazyVar
import com.simprints.libsimprints.FingerIdentifier
import com.simprints.mockscanner.MockBluetoothAdapter
import com.simprints.mockscanner.MockFinger
import com.simprints.mockscanner.MockScannerManager
import io.reactivex.observers.TestObserver
import io.realm.RealmConfiguration
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@SmallTest
class SessionEventsUploaderTaskEndToEndTest : DaggerForAndroidTests(), FirstUseLocalAndRemote {

    override var peopleRealmConfiguration: RealmConfiguration? = null
    override var sessionsRealmConfiguration: RealmConfiguration? = null

    override lateinit var testProject: TestProject

    @Rule
    @JvmField
    val simprintsActionTestRule = ActivityTestRule(CheckLoginFromIntentActivity::class.java, false, false)

    @Inject lateinit var realmSessionEventsManager: SessionEventsLocalDbManager
    @Inject lateinit var sessionEventsManager: SessionEventsManager
    @Inject lateinit var settingsPreferencesManagerSpy: SettingsPreferencesManager
    @Inject lateinit var remoteDbManager: RemoteDbManager
    @Inject lateinit var timeHelper: TimeHelper
    @Inject lateinit var randomGeneratorMock: RandomGenerator

    override var preferencesModule: PreferencesModuleForAnyTests by lazyVar {
        PreferencesModuleForAnyTests(settingsPreferencesManagerRule = DependencyRule.SpyRule)
    }

    override var module by lazyVar {
        AppModuleForAndroidTests(
            app,
            randomGeneratorRule = DependencyRule.MockRule,
            bluetoothComponentAdapterRule = DependencyRule.ReplaceRule { mockBluetoothAdapter }
        )
    }

    private lateinit var mockBluetoothAdapter: MockBluetoothAdapter

    @Before
    override fun setUp() {
        app = InstrumentationRegistry.getTargetContext().applicationContext as Application
        super<DaggerForAndroidTests>.setUp()

        testAppComponent.inject(this)

        setupRandomGeneratorToGenerateKey(DEFAULT_REALM_KEY, randomGeneratorMock)
        app.initDependencies()

        whenever(settingsPreferencesManagerSpy.fingerStatus).thenReturn(hashMapOf(
            FingerIdentifier.LEFT_THUMB to true,
            FingerIdentifier.LEFT_INDEX_FINGER to true))

        super<FirstUseLocalAndRemote>.setUp()
        signOut()
    }

    @Test
    fun closeSessions_shouldGetUploaded() {
        mockBluetoothAdapter = MockBluetoothAdapter(MockScannerManager(mockFingers = arrayOf(*MockFinger.person1TwoFingersGoodScan)))

        launchActivityEnrol(testProject.toCalloutCredentials(), simprintsActionTestRule)
        enterCredentialsDirectly(testProject.toCalloutCredentials(), testProject.secret)
        pressSignIn()
        setupActivityAndContinue()

        val nSession = BATCH_SIZE + 1
        createClosedSessions(nSession).forEach {
            realmSessionEventsManager.insertOrUpdateSessionEvents(it).blockingAwait()
        }

        val testObserver = executeUpload()
        testObserver.waitForCompletionAndAssertNoErrors()

        val response = RemoteTestingManager.create().getSessionCount(testProject.id)
        Truth.assertThat(response.count).isEqualTo(nSession)
    }

    @After
    override fun tearDown() {
        super.tearDown()
    }

    private fun executeUpload(): TestObserver<Void> {
        val syncTask = SessionEventsUploaderTask(
            testProject.id,
            realmSessionEventsManager.loadSessions().blockingGet().map { it.id }.toTypedArray(),
            sessionEventsManager,
            timeHelper,
            remoteDbManager.getSessionsApiClient().blockingGet())

        return syncTask.execute().test()
    }

    private fun createClosedSessions(nSessions: Int) =
        mutableListOf<SessionEvents>().apply {
            repeat(nSessions) { this.add(createFakeClosedSession(timeHelper, testProject.id)) }
        }

    private fun signOut() {
        remoteDbManager.signOutOfRemoteDb()
    }
}
