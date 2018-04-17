package com.simprints.id.activities.dashboard

import com.simprints.id.Application
import com.simprints.id.BuildConfig
import com.simprints.id.R
import com.simprints.id.activities.dashboard.models.DashboardCard
import com.simprints.id.activities.dashboard.models.DashboardCardType
import com.simprints.id.domain.Project
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.testUtils.anyNotNull
import com.simprints.id.testUtils.roboletric.*
import com.simprints.id.testUtils.whenever
import com.simprints.id.tools.utils.AndroidResourcesHelperImpl
import io.reactivex.Single
import junit.framework.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.util.*

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class, application = TestApplication::class)
class DashboardCardsFactoryTest {

    private lateinit var app: Application

    @Before
    fun setUp() {
        app = (RuntimeEnvironment.application as Application)
        mockLocalDbManager(app)
        mockRemoteDbManager(app)
        mockIsSignedIn(app, getRoboSharedPreferences())

        mockDbManager(app)
    }

    @Test
    fun shouldCreateTheProjectCard_onlyWhenItHasAValidProject() {
        val project = Project().apply { name = "project name"; description = "project desc" }
        whenever(app.localDbManager.loadProjectFromLocal(anyNotNull())).thenReturn(Single.just(project))
        whenever(app.remoteDbManager.loadProjectFromRemote(anyNotNull())).thenReturn(Single.just(project))

        mockNPeopleForSyncRequest(app.remoteDbManager, 0)

        val factory = DashboardCardsFactory(app.dataManager, AndroidResourcesHelperImpl(app))
        val card = getCardIfCreated(factory, "project name")
        Assert.assertEquals(card?.description, "project desc")

        whenever(app.localDbManager.loadProjectFromLocal(anyNotNull())).thenReturn(null)
        whenever(app.remoteDbManager.loadProjectFromRemote(anyNotNull())).thenReturn(Single.error(Exception("force failing")))

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
        val factory = DashboardCardsFactory(app.dataManager, AndroidResourcesHelperImpl(app))
        val lastEnrolDate = Date()
        assertThatCardEventsAreCreatedOnlyWhenRequired(
            factory,
            { factory.dateFormat.format(lastEnrolDate).also { app.dataManager.lastEnrolDate = lastEnrolDate } },
            { app.dataManager.lastEnrolDate = null },
            app.getString(R.string.dashboard_card_enrol_title))
    }

    @Test
    fun shouldCreateTheLastIdentificationCard_onlyWhenAnIdentificationEventHappened() {
        val factory = DashboardCardsFactory(app.dataManager, AndroidResourcesHelperImpl(app))
        val lastIdentificationDate = Date()
        assertThatCardEventsAreCreatedOnlyWhenRequired(
            factory,
            { factory.dateFormat.format(lastIdentificationDate).also { app.dataManager.lastIdentificationDate = lastIdentificationDate } },
            { app.dataManager.lastIdentificationDate = null },
            app.getString(R.string.dashboard_card_identification_title))
    }

    @Test
    fun shouldCreateTheLastVerificationCard_onlyWhenAnVerificationEventHappened() {
        val factory = DashboardCardsFactory(app.dataManager, AndroidResourcesHelperImpl(app))
        val lastVerificationDate = Date()
        assertThatCardEventsAreCreatedOnlyWhenRequired(
            factory,
            { factory.dateFormat.format(lastVerificationDate).also { app.dataManager.lastVerificationDate = lastVerificationDate } },
            { app.dataManager.lastVerificationDate = null },
            app.getString(R.string.dashboard_card_verification_title))
    }

    @Test
    fun shouldCreateTheLastUserCard_onlyWhenAnLastUserEventHappened() {
        val factory = DashboardCardsFactory(app.dataManager, AndroidResourcesHelperImpl(app))
        val lastUser = "someone"
        assertThatCardEventsAreCreatedOnlyWhenRequired(
            factory,
            { lastUser.also { app.dataManager.lastUserUsed = lastUser } },
            { app.dataManager.lastUserUsed = "" },
            app.getString(R.string.dashboard_card_lastuser_title))
    }

    @Test
    fun shouldCreateTheLasScannerCard_onlyWhenAnLasScannerEventHappened() {
        val factory = DashboardCardsFactory(app.dataManager, AndroidResourcesHelperImpl(app))
        val lastScanner = "SPXXXX"
        assertThatCardEventsAreCreatedOnlyWhenRequired(
            factory,
            { lastScanner.also { app.dataManager.lastScannerUsed = lastScanner } },
            { app.dataManager.lastScannerUsed = "" },
            app.getString(R.string.dashboard_card_lastscanner_title))
    }

    private fun assertThatCardEventsAreCreatedOnlyWhenRequired(cardsFactory: DashboardCardsFactory,
                                                               createEvent: () -> Any,
                                                               deleteEvent: () -> Unit,
                                                               cardTitle: String) {
        val event = createEvent()
        whenever(app.remoteDbManager.loadProjectFromRemote(anyNotNull())).thenReturn(Single.just(Project().apply { description = ""; name = "" }))
        mockNPeopleForSyncRequest(app.remoteDbManager, 0)

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
}
