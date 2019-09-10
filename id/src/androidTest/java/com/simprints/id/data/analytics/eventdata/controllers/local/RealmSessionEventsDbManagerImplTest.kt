package com.simprints.id.data.analytics.eventdata.controllers.local

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.simprints.id.data.db.local.models.LocalDbKey
import com.simprints.id.data.secure.SecureDataManager
import com.simprints.id.testtools.AndroidTestConfig
import com.simprints.id.tools.TimeHelper
import com.simprints.testtools.common.syntax.anyNotNull
import com.simprints.testtools.common.syntax.mock
import com.simprints.testtools.common.syntax.whenever
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject
import kotlin.random.Random

@RunWith(AndroidJUnit4::class)
class RealmSessionEventsDbManagerImplTest { // TODO : Tests are failing because creating a project remotely is throwing a 404

    private lateinit var realmSessionEventsManager: SessionEventsLocalDbManager
    private val realmForDataEvent
        get() = (realmSessionEventsManager as RealmSessionEventsDbManagerImpl).getRealmInstance().blockingGet()

    private var localDbKey: LocalDbKey = LocalDbKey("database_name", Random.nextBytes(64))

    private val testProjectId1 = "test_project1"
    private val testProjectId2 = "test_project2"
    private val testProjectId3 = "test_project3"

    @Inject lateinit var timeHelper: TimeHelper

    @Before
    fun setUp() {
        AndroidTestConfig(this).fullSetup()

        val secureDataManager: SecureDataManager = mock()
        whenever(secureDataManager) { getLocalDbKeyOrThrow(anyNotNull()) } thenReturn localDbKey
        realmSessionEventsManager = RealmSessionEventsDbManagerImpl(ApplicationProvider.getApplicationContext(), secureDataManager)
    }

    @Test
    fun deleteSessions_shouldCleanDb() {
        verifyNumberOfSessionsInDb(0, realmForDataEvent)

        val sessionOpenProject1Id = createAndSaveOpenSession()
        val sessionCloseProject1Id = createAndSaveCloseSession()
        val sessionCloseProject2Id = createAndSaveCloseSession(testProjectId2)
        createAndSaveCloseSession(testProjectId3)
        createAndSaveCloseSession(testProjectId3)

        verifyNumberOfSessionsInDb(5, realmForDataEvent)

        realmSessionEventsManager.deleteSessions(projectId = testProjectId3).blockingAwait()
        verifySessionsStoredInDb(sessionOpenProject1Id, sessionCloseProject1Id, sessionCloseProject2Id)

        realmSessionEventsManager.deleteSessions(openSession = true).blockingAwait()
        verifySessionsStoredInDb(sessionCloseProject1Id, sessionCloseProject2Id)

        realmSessionEventsManager.deleteSessions(openSession = false).blockingAwait()

        verifyNumberOfSessionsInDb(0, realmForDataEvent)
        verifyNumberOfEventsInDb(0, realmForDataEvent)
        verifyNumberOfDatabaseInfosInDb(0, realmForDataEvent)
        verifyNumberOfLocationsInDb(0, realmForDataEvent)
        verifyNumberOfDeviceInfosInDb(0, realmForDataEvent)
    }

    private fun verifySessionsStoredInDb(vararg sessionsIds: String) {
        with(realmSessionEventsManager.loadSessions().blockingGet()) {
            val ids = this.map { it.id }
            Truth.assertThat(ids).containsExactlyElementsIn(sessionsIds)
        }
    }

    private fun createAndSaveCloseSession(projectId: String = testProjectId1): String =
        createAndSaveCloseFakeSession(timeHelper, realmSessionEventsManager, projectId)

    private fun createAndSaveOpenSession(projectId: String = testProjectId1): String =
        createAndSaveOpenFakeSession(timeHelper, realmSessionEventsManager, projectId)
}
