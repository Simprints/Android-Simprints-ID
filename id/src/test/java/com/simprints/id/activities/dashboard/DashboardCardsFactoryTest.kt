package com.simprints.id.activities.dashboard

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.id.R
import com.simprints.id.activities.dashboard.viewModels.DashboardCardType
import com.simprints.id.activities.dashboard.viewModels.DashboardCardViewModel
import com.simprints.id.commontesttools.di.TestAppModule
import com.simprints.id.data.db.syncstatus.SyncStatusDatabase
import com.simprints.id.data.db.common.RemoteDbManager
import com.simprints.id.data.db.person.remote.PersonRemoteDataSource
import com.simprints.id.data.db.project.remote.ProjectRemoteDataSource
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.prefs.PreferencesManagerImpl
import com.simprints.id.data.db.person.domain.PeopleCount
import com.simprints.id.domain.modality.Modes
import com.simprints.id.testtools.TestApplication
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.id.testtools.state.RobolectricTestMocker
import com.simprints.testtools.common.di.DependencyRule.MockRule
import com.simprints.testtools.common.syntax.anyNotNull
import com.simprints.testtools.common.syntax.anyOrNull
import com.simprints.testtools.common.syntax.mock
import com.simprints.testtools.common.syntax.whenever
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import com.simprints.testtools.unit.robolectric.getSharedPreferences
import io.reactivex.Single
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.util.*
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class DashboardCardsFactoryTest {

    private val app = ApplicationProvider.getApplicationContext() as TestApplication

    @Inject lateinit var remoteDbManagerMock: RemoteDbManager
    @Inject lateinit var personRemoteDataSourceMock: PersonRemoteDataSource
    @Inject lateinit var remoteProjectManagerMock: ProjectRemoteDataSource
    @Inject lateinit var localDbManagerMock: LocalDbManager
    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var loginInfoManager: LoginInfoManager
    @Inject lateinit var syncStatusDatabase: SyncStatusDatabase

    private val module by lazy {
        TestAppModule(app,
            remoteDbManagerRule = MockRule,
            remotePeopleManagerRule = MockRule,
            remoteProjectManagerRule = MockRule,
            localDbManagerRule = MockRule,
            syncStatusDatabaseRule = MockRule)
    }

    @Before
    fun setUp() {
        UnitTestConfig(this, module).fullSetup()

        whenever(syncStatusDatabase.downSyncDao).thenReturn(mock())
        whenever(syncStatusDatabase.upSyncDao).thenReturn(mock())

        whenever(syncStatusDatabase.downSyncDao.getDownSyncStatusLiveData()).thenReturn(mock())
        whenever(syncStatusDatabase.downSyncDao.insertOrReplaceDownSyncStatus(anyNotNull())).then { }
        whenever(syncStatusDatabase.upSyncDao.getUpSyncStatus()).thenReturn(mock())

        RobolectricTestMocker
            .initLogInStateMock(getSharedPreferences(PreferencesManagerImpl.PREF_FILE_NAME), remoteDbManagerMock)
            .setUserLogInState(true, getSharedPreferences(PreferencesManagerImpl.PREF_FILE_NAME), userId = "userId")
            .mockLoadProject(localDbManagerMock, remoteProjectManagerMock)
    }

    @Test
    fun shouldCreateTheProjectCard_onlyWhenItHasAValidProject() {
        val factory = DashboardCardsFactory(app.component)

        val card = getCardIfCreated(factory, "project name")
        Assert.assertEquals(card?.description, "project desc")

        whenever(localDbManagerMock.loadProjectFromLocal(anyNotNull())).thenReturn(Single.error(Exception("force failing")))
        whenever(remoteProjectManagerMock.loadProjectFromRemote(anyNotNull())).thenReturn(Single.error(Exception("force failing")))

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
        val factory = DashboardCardsFactory(app.component)
        val lastEnrolDate = Date()
        assertThatCardEventsAreCreatedOnlyWhenRequired(
            factory,
            { factory.dateFormat.format(lastEnrolDate).also { preferencesManager.lastEnrolDate = lastEnrolDate } },
            { preferencesManager.lastEnrolDate = null },
            app.getString(R.string.dashboard_card_enrol_title))
    }

    @Test
    fun shouldCreateTheLastIdentificationCard_onlyWhenAnIdentificationEventHappened() {
        val factory = DashboardCardsFactory(app.component)
        val lastIdentificationDate = Date()
        assertThatCardEventsAreCreatedOnlyWhenRequired(
            factory,
            { factory.dateFormat.format(lastIdentificationDate).also { preferencesManager.lastIdentificationDate = lastIdentificationDate } },
            { preferencesManager.lastIdentificationDate = null },
            app.getString(R.string.dashboard_card_identification_title))
    }

    @Test
    fun shouldCreateTheLastVerificationCard_onlyWhenAnVerificationEventHappened() {
        val factory = DashboardCardsFactory(app.component)
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
        val factory = DashboardCardsFactory(app.component)
        val signedInUser = "someone"
        assertThatCardEventsAreCreatedOnlyWhenRequired(
            factory,
            { signedInUser.also { loginInfoManager.signedInUserId = signedInUser } },
            { loginInfoManager.signedInUserId = "" },
            app.getString(R.string.dashboard_card_currentuser_title))
    }

    @Test
    fun shouldCreateTheLastScannerCard_onlyWhenALastScannerEventHappened() {
        val factory = DashboardCardsFactory(app.component)
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
        mockNPeopleForSyncRequest(personRemoteDataSourceMock, getMockListOfPeopleCountWithCounter(0))

        var card = getCardIfCreated(
            cardsFactory,
            cardTitle)

        Assert.assertNotNull(card)
        Assert.assertEquals(card!!.description, event)

        deleteEvent()
        card = getCardIfCreated(cardsFactory, cardTitle)
        Assert.assertNull(card)
    }

    private fun getCardIfCreated(cardsFactory: DashboardCardsFactory, title: String?): DashboardCardViewModel.State? {
        mockNPeopleForSyncRequest(personRemoteDataSourceMock, getMockListOfPeopleCountWithCounter(0))
        mockNLocalPeople(localDbManagerMock, 0)

        val testObserver = Single.merge(cardsFactory.createCards()).test()
        testObserver.awaitTerminalEvent()
        testObserver
            .assertNoErrors()
            .assertComplete()

        return try {
            val cardsViewModels = testObserver.values().filterIsInstance(DashboardCardViewModel::class.java)
            return cardsViewModels.first {
                it.stateLiveData.value?.title == title
            }.stateLiveData.value
        } catch (e: Exception) {
            null
        }
    }

    private fun getMockListOfPeopleCountWithCounter(counter: Int) =
        listOf(PeopleCount("projectId", "userId", "0", listOf(Modes.FACE, Modes.FINGERPRINT), counter))

    private fun mockNPeopleForSyncRequest(personRemoteDataSource: PersonRemoteDataSource, peopleCounts: List<PeopleCount>) {
        whenever(personRemoteDataSource.getDownSyncPeopleCount(anyNotNull())).thenReturn(Single.just(peopleCounts))
    }

    private fun mockNLocalPeople(localDbManager: LocalDbManager, nLocalPeople: Int) {
        whenever(localDbManager.getPeopleCountFromLocal(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())).thenReturn(Single.just(nLocalPeople))
    }
}
