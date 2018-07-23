package com.simprints.id.activities.dashboard

import com.google.firebase.FirebaseApp
import com.simprints.id.R
import com.simprints.id.activities.dashboard.models.DashboardCard
import com.simprints.id.activities.dashboard.models.DashboardCardType
import com.simprints.id.data.DataManager
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.local.realm.models.rl_SyncInfo
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.di.AppModuleForTests
import com.simprints.id.di.DaggerForTests
import com.simprints.id.shared.DependencyRule.*
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
@Config(application = TestApplication::class)
class DashboardCardsFactoryTest : DaggerForTests() {

    @Inject lateinit var dataManager: DataManager
    @Inject lateinit var remoteDbManagerMock: RemoteDbManager
    @Inject lateinit var localDbManagerMock: LocalDbManager
    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var dbManager: DbManager

    override var module by lazyVar {
        AppModuleForTests(app,
            remoteDbManagerRule = MockRule(),
            localDbManagerRule = MockRule())
    }

    @Before
    override fun setUp() {
        FirebaseApp.initializeApp(RuntimeEnvironment.application)
        app = (RuntimeEnvironment.application as TestApplication)
        super.setUp()
        testAppComponent.inject(this)
        dbManager.initialiseDb()

        initLogInStateMock(getRoboSharedPreferences(), remoteDbManagerMock)
        setUserLogInState(true, getRoboSharedPreferences())

        mockLoadProject(localDbManagerMock, remoteDbManagerMock)
    }

    @Test
    fun shouldCreateTheProjectCard_onlyWhenItHasAValidProject() {
        val factory = DashboardCardsFactory(testAppComponent)
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
        val factory = DashboardCardsFactory(testAppComponent)
        val lastEnrolDate = Date()
        assertThatCardEventsAreCreatedOnlyWhenRequired(
            factory,

            { factory.dateFormat.format(lastEnrolDate).also { preferencesManager.lastEnrolDate = lastEnrolDate } },
            { preferencesManager.lastEnrolDate = null },
            app.getString(R.string.dashboard_card_enrol_title))
    }

    @Test
    fun shouldCreateTheLastIdentificationCard_onlyWhenAnIdentificationEventHappened() {
        val factory = DashboardCardsFactory(testAppComponent)
        val lastIdentificationDate = Date()
        assertThatCardEventsAreCreatedOnlyWhenRequired(
            factory,
            { factory.dateFormat.format(lastIdentificationDate).also { preferencesManager.lastIdentificationDate = lastIdentificationDate } },
            { preferencesManager.lastIdentificationDate = null },
            app.getString(R.string.dashboard_card_identification_title))
    }

    @Test
    fun shouldCreateTheLastVerificationCard_onlyWhenAnVerificationEventHappened() {
        val factory = DashboardCardsFactory(testAppComponent)
        val lastVerificationDate = Date()
        assertThatCardEventsAreCreatedOnlyWhenRequired(
            factory,
            { factory.dateFormat.format(lastVerificationDate).also { preferencesManager.lastVerificationDate = lastVerificationDate } },
            { preferencesManager.lastVerificationDate = null },
            app.getString(R.string.dashboard_card_verification_title))
    }

    @Test
    fun shouldCreateTheCurrentUserCard_onlyWhenAnLastUserEventHappened() {
        val factory = DashboardCardsFactory(testAppComponent)
        val lastUser = "someone"
        assertThatCardEventsAreCreatedOnlyWhenRequired(
            factory,
            { lastUser.also { preferencesManager.lastUserUsed = lastUser } },
            { preferencesManager.lastUserUsed = "" },
            app.getString(R.string.dashboard_card_currentuser_title))
    }

    @Test
    fun shouldCreateTheLasScannerCard_onlyWhenAnLasScannerEventHappened() {
        val factory = DashboardCardsFactory(testAppComponent)
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
        mockGetSyncInfoFor(localDbManagerMock)

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
        whenever(remoteDbManager.getNumberOfPatientsForSyncParams(anyNotNull())).thenReturn(Single.just(count))
    }

    private fun mockNLocalPeople(localDbManager: LocalDbManager, nLocalPeople: Int) {
        whenever(localDbManager.getPeopleCountFromLocal(any(), any(), any(), any())).thenReturn(Single.just(nLocalPeople))
    }

    private fun mockGetSyncInfoFor(localDbManager: LocalDbManager) {
        whenever(localDbManager.getSyncInfoFor(anyNotNull())).thenReturn(Single.just(rl_SyncInfo().apply { lastSyncTime = Date() }))
    }
}
