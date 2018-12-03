package com.simprints.id.activities.dashboard

import androidx.work.Configuration
import androidx.work.WorkManager
import com.google.firebase.FirebaseApp
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.spy
import com.simprints.id.R
import com.simprints.id.activities.ShadowAndroidXMultiDex
import com.simprints.id.activities.dashboard.models.DashboardCard
import com.simprints.id.activities.dashboard.models.DashboardCardType
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.di.AppModuleForTests
import com.simprints.id.di.DaggerForTests
import com.simprints.id.services.scheduledSync.peopleDownSync.SyncStatusDatabase
import com.simprints.id.data.db.local.room.DownSyncDao
import com.simprints.id.shared.DependencyRule.MockRule
import com.simprints.id.shared.anyNotNull
import com.simprints.id.shared.whenever
import com.simprints.id.testUtils.roboletric.*
import com.simprints.id.tools.delegates.lazyVar
import io.reactivex.Single
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.util.*
import javax.inject.Inject

@RunWith(RobolectricTestRunner::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class DashboardCardsFactoryTest : DaggerForTests() {

    @Inject lateinit var remoteDbManagerMock: RemoteDbManager
    @Inject lateinit var localDbManagerMock: LocalDbManager
    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var loginInfoManager: LoginInfoManager
    @Inject lateinit var dbManager: DbManager
    @Inject lateinit var syncStatusDatabase: SyncStatusDatabase

    private lateinit var syncStatusDatabaseModel: DownSyncDao

    override var module by lazyVar {
        AppModuleForTests(app,
            remoteDbManagerRule = MockRule,
            localDbManagerRule = MockRule,
            syncStatusDatabaseRule = MockRule)
    }

    val activity = spy<DashboardActivity>()

    @Before
    override fun setUp() {
        FirebaseApp.initializeApp(RuntimeEnvironment.application)
        app = (RuntimeEnvironment.application as TestApplication)
        super.setUp()
        testAppComponent.inject(this)
        dbManager.initialiseDb()
        WorkManager.initialize(app, Configuration.Builder().build())

        whenever(syncStatusDatabase.downSyncDao).thenReturn(mock())
        syncStatusDatabaseModel = syncStatusDatabase.downSyncDao

        whenever(syncStatusDatabaseModel.getDownSyncStatus()).thenReturn(mock())
        whenever(syncStatusDatabaseModel.insertOrReplaceDownSyncStatus(anyNotNull())).then { }

        initLogInStateMock(getRoboSharedPreferences(), remoteDbManagerMock)
        setUserLogInState(true, getRoboSharedPreferences())

        mockLoadProject(localDbManagerMock, remoteDbManagerMock)
    }

    @Test
    fun shouldCreateTheProjectCard_onlyWhenItHasAValidProject() {
        val factory = DashboardCardsFactory(activity, testAppComponent)

        val card = getCardIfCreated(factory, "project name")
        Assert.assertEquals(card?.description, "project desc")

        whenever(localDbManagerMock.loadProjectFromLocal(anyNotNull())).thenReturn(Single.error(Exception("force failing")))
        whenever(remoteDbManagerMock.loadProjectFromRemote(anyNotNull())).thenReturn(Single.error(Exception("force failing")))

        val testObserver = Single.merge(factory.createCards()).test()
        testObserver.awaitTerminalEvent()
        try {
            testObserver.values().first { it.type == DashboardCardType.LOCAL_DB }
        } catch (e: Exception) {
            Assert.assertTrue(true)
        }
    }

    @Test
    fun shouldCreateTheLastEnrolCard_onlyWhenAnEnrolEventHappened() {
        val factory = DashboardCardsFactory(activity, testAppComponent)
        val lastEnrolDate = Date()
        assertThatCardEventsAreCreatedOnlyWhenRequired(
            factory,

            { factory.dateFormat.format(lastEnrolDate).also { preferencesManager.lastEnrolDate = lastEnrolDate } },
            { preferencesManager.lastEnrolDate = null },
            app.getString(R.string.dashboard_card_enrol_title))
    }

    @Test
    fun shouldCreateTheLastIdentificationCard_onlyWhenAnIdentificationEventHappened() {
        val factory = DashboardCardsFactory(activity, testAppComponent)
        val lastIdentificationDate = Date()
        assertThatCardEventsAreCreatedOnlyWhenRequired(
            factory,
            { factory.dateFormat.format(lastIdentificationDate).also { preferencesManager.lastIdentificationDate = lastIdentificationDate } },
            { preferencesManager.lastIdentificationDate = null },
            app.getString(R.string.dashboard_card_identification_title))
    }

    @Test
    fun shouldCreateTheLastVerificationCard_onlyWhenAnVerificationEventHappened() {
        val factory = DashboardCardsFactory(activity, testAppComponent)
        val lastVerificationDate = Date()
        assertThatCardEventsAreCreatedOnlyWhenRequired(
            factory,
            { factory.dateFormat.format(lastVerificationDate).also { preferencesManager.lastVerificationDate = lastVerificationDate } },
            { preferencesManager.lastVerificationDate = null },
            app.getString(R.string.dashboard_card_verification_title))
    }

    @Test
    @Config(sdk = [25]) // Bug with Robolectric and SharedPreferences.commit() on API >= 26. apply() works fine
    fun shouldCreateTheCurrentUserCard_onlyIfValidUserSignedIn() {
        val factory = DashboardCardsFactory(activity, testAppComponent)
        val signedInUser = "someone"
        assertThatCardEventsAreCreatedOnlyWhenRequired(
            factory,
            { signedInUser.also { loginInfoManager.signedInUserId = signedInUser } },
            { loginInfoManager.signedInUserId = "" },
            app.getString(R.string.dashboard_card_currentuser_title))
    }

    @Test
    fun shouldCreateTheLastScannerCard_onlyWhenALastScannerEventHappened() {
        val factory = DashboardCardsFactory(activity, testAppComponent)
        val lastScanner = "SPXXXX"
        assertThatCardEventsAreCreatedOnlyWhenRequired(
            factory,
            { lastScanner.also { preferencesManager.lastScannerUsed = lastScanner } },
            { preferencesManager.lastScannerUsed = "" },
            app.getString(R.string.dashboard_card_lastscanner_title))
    }

    private fun assertThatCardEventsAreCreatedOnlyWhenRequired(cardsFactory: DashboardCardsFactory,
                                                               createEvent: () -> Any,
                                                               deleteEvent: () -> Unit,
                                                               cardTitle: String) {
        val event = createEvent()
        mockNPeopleForSyncRequest(remoteDbManagerMock, 0)

        var card = getCardIfCreated(
            cardsFactory,
            cardTitle)

        Assert.assertNotNull(card)
        Assert.assertEquals(card!!.description, event)

        deleteEvent()
        card = getCardIfCreated(cardsFactory, cardTitle)
        Assert.assertNull(card)
    }

    private fun getCardIfCreated(cardsFactory: DashboardCardsFactory, title: String?): DashboardCard? {
        mockNPeopleForSyncRequest(remoteDbManagerMock, 0)
        mockNLocalPeople(localDbManagerMock, 0)

        val testObserver = Single.merge(cardsFactory.createCards()).test()
        testObserver.awaitTerminalEvent()
        testObserver
            .assertNoErrors()
            .assertComplete()
        return try {
            return testObserver.values().first { it.title == title }
        } catch (e: Exception) {
            null
        }
    }

    private fun mockNPeopleForSyncRequest(remoteDbManager: RemoteDbManager, count: Int) {
        whenever(remoteDbManager.getNumberOfPatientsForSyncScope(anyNotNull())).thenReturn(Single.just(count))
    }

    private fun mockNLocalPeople(localDbManager: LocalDbManager, nLocalPeople: Int) {
        whenever(localDbManager.getPeopleCountFromLocal(any(), any(), any(), any())).thenReturn(Single.just(nLocalPeople))
    }
}
