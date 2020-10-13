package com.simprints.id.data.db.subject.migration

import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.commontesttools.DefaultTestConstants.defaultSubject
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.db.event.EventRepository
import com.simprints.id.data.db.event.domain.models.subject.EnrolmentRecordCreationEvent
import com.simprints.id.data.db.subject.domain.SubjectAction.Creation
import com.simprints.id.data.db.subject.local.SubjectLocalDataSource
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.modality.toMode
import com.simprints.id.exceptions.unexpected.MigrationToNewEventArchitectureException
import com.simprints.id.tools.mockUUID
import com.simprints.id.tools.time.TimeHelper
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class SubjectToEventMigrationManagerImplTest {

    @MockK lateinit var loginInfoManager: LoginInfoManager
    @MockK lateinit var eventRepository: EventRepository
    @MockK lateinit var timeHelper: TimeHelper
    @MockK lateinit var crashReportManager: CrashReportManager
    @MockK lateinit var preferencesManager: PreferencesManager
    @MockK lateinit var localDataSource: SubjectLocalDataSource

    private lateinit var migrationManager: SubjectToEventMigrationManager

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        migrationManager = SubjectToEventMigrationManagerImpl(
            loginInfoManager,
            eventRepository,
            timeHelper,
            crashReportManager,
            preferencesManager,
            localDataSource)

        mockUUID()
    }

    @Test
    fun userNotSignedIn_migrationShouldNotHappen() {
        runBlockingTest {
            mockUserIsNotSignedIn()

            migrationManager.migrateSubjectToSyncToEventsDb()

            coVerify(exactly = 0) { eventRepository.addEvent(any()) }
        }
    }

    @Test
    fun userSignedIn_migrationShouldStart() {
        runBlockingTest {
            mockUserIsSignedIn()

            migrationManager.migrateSubjectToSyncToEventsDb()

            coVerify(exactly = 1) { localDataSource.load(any()) }
        }
    }

    @Test
    fun noSubjectsInTheOldDb_nothingToMigrate() {
        runBlockingTest {
            mockUserIsSignedIn()
            coEvery { localDataSource.load(any()) } returns emptyFlow()

            migrationManager.migrateSubjectToSyncToEventsDb()

            coVerify(exactly = 1) { localDataSource.load(any()) }
        }
    }

    @Test
    fun subjectsToSyncInTheOldDb_shouldGetMigratedToEvents() {
        runBlockingTest {
            mockUserIsSignedIn()
            val subjectIntoDb = defaultSubject.copy(toSync = true)
            coEvery { localDataSource.load(any()) } returns flowOf(subjectIntoDb)

            migrationManager.migrateSubjectToSyncToEventsDb()

            val expectedEvent = EnrolmentRecordCreationEvent(
                timeHelper.now(),
                subjectIntoDb.subjectId,
                subjectIntoDb.projectId,
                subjectIntoDb.moduleId,
                subjectIntoDb.attendantId,
                preferencesManager.modalities.map { it.toMode() },
                EnrolmentRecordCreationEvent.buildBiometricReferences(subjectIntoDb.fingerprintSamples, subjectIntoDb.faceSamples)
            )

            coVerify(exactly = 1) {
                eventRepository.addEvent(expectedEvent)
            }
        }
    }

    @Test
    fun migrateMultiTimes_shouldNotHaveAnyEffect() {
        runBlockingTest {
            mockUserIsSignedIn()
            val subjectIntoDb = defaultSubject.copy(toSync = true)
            coEvery { localDataSource.load(any()) } returnsMany listOf(flowOf(subjectIntoDb), emptyFlow())

            migrationManager.migrateSubjectToSyncToEventsDb()
            migrationManager.migrateSubjectToSyncToEventsDb()

            coVerify(exactly = 1) { eventRepository.addEvent(any()) }
            coVerify(exactly = 1) {
                localDataSource.performActions(listOf(Creation(subjectIntoDb.copy(toSync = false))))
            }
        }
    }


    @Test
    fun subjectsToSyncInTheOldDb_shouldOverrideSyncFlag() {
        runBlockingTest {
            mockUserIsSignedIn()
            val subjectIntoDb = defaultSubject.copy(toSync = true)
            coEvery { localDataSource.load(any()) } returns flowOf(subjectIntoDb)

            migrationManager.migrateSubjectToSyncToEventsDb()

            coVerify(exactly = 1) {
                localDataSource.performActions(listOf(Creation(subjectIntoDb.copy(toSync = false))))
            }
        }
    }

    @Test
    fun migrationFails_shouldReportTheIssue() {
        runBlockingTest {
            mockUserIsSignedIn()
            coEvery { loginInfoManager.getSignedInProjectIdOrEmpty() } throws Throwable("test")

            migrationManager.migrateSubjectToSyncToEventsDb()

            verify(exactly = 1) { crashReportManager.logException(any<MigrationToNewEventArchitectureException>()) }
        }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    private fun mockUserIsNotSignedIn() {
        every { loginInfoManager.getSignedInProjectIdOrEmpty() } returns ""
    }

    private fun mockUserIsSignedIn() {
        every { loginInfoManager.getSignedInProjectIdOrEmpty() } returns DEFAULT_PROJECT_ID
    }
}
