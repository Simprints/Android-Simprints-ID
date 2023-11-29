package com.simprints.infra.authlogic.worker

import com.simprints.infra.authlogic.authenticator.SignerManager
import com.simprints.infra.enrolment.records.store.EnrolmentRecordRepository
import com.simprints.infra.enrolment.records.sync.worker.EnrolmentRecordScheduler
import com.simprints.infra.events.EventRepository
import com.simprints.infra.eventsync.EventSyncManager
import com.simprints.infra.images.ImageRepository
import com.simprints.infra.projectsecuritystore.securitystate.models.SecurityState
import com.simprints.infra.projectsecuritystore.securitystate.models.UpSyncEnrolmentRecords
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

internal class SecurityStateProcessorTest {

    @MockK
    lateinit var mockImageRepository: ImageRepository

    @MockK
    lateinit var mockSignerManager: SignerManager

    @MockK
    lateinit var enrolmentRecordRepository: EnrolmentRecordRepository

    @MockK
    lateinit var enrolmentRecordScheduler: EnrolmentRecordScheduler

    @MockK
    lateinit var eventRepository: EventRepository

    @MockK
    lateinit var eventSyncManager: EventSyncManager

    private lateinit var securityStateProcessor: SecurityStateProcessor

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        coEvery { mockSignerManager.signedInProjectId } returns PROJECT_ID
        coEvery { eventSyncManager.countEventsToUpload(PROJECT_ID, null) } returns flowOf(0)
        securityStateProcessor = SecurityStateProcessor(
            imageRepository = mockImageRepository,
            enrolmentRecordRepository = enrolmentRecordRepository,
            enrolmentRecordScheduler = enrolmentRecordScheduler,
            eventRepository = eventRepository,
            eventSyncManager = eventSyncManager,
            signerManager = mockSignerManager,
        )
    }

    @Test
    fun `when there is an instruction it should schedule a work to upload the enrolment records`() =
        runTest {
            val securityState =
                SecurityState(
                    DEVICE_ID,
                    SecurityState.Status.RUNNING,
                    UpSyncEnrolmentRecords(PROJECT_ID, SUBJECTS)
                )

            securityStateProcessor.processSecurityState(securityState)

            coVerify(exactly = 1) {
                enrolmentRecordScheduler.upload(id = PROJECT_ID, subjectIds = SUBJECTS)
            }
        }

    @Test
    fun withRunningSecurityState_shouldDoNothing() = runTest {
        val status = SecurityState.Status.RUNNING
        val securityState = SecurityState(DEVICE_ID, status)

        securityStateProcessor.processSecurityState(securityState)

        coVerify(exactly = 0) { mockImageRepository.deleteStoredImages() }
        coVerify(exactly = 0) { enrolmentRecordRepository.deleteAll() }
        coVerify(exactly = 0) { eventRepository.deleteAll() }
        coVerify(exactly = 0) { mockSignerManager.signOut() }
    }

    @Test
    fun withCompromisedSecurityState_shouldDeleteLocalDataAndSignOut() = runTest {
        val status = SecurityState.Status.COMPROMISED
        val securityState = SecurityState(DEVICE_ID, status)

        securityStateProcessor.processSecurityState(securityState)

        coVerify(exactly = 1) { mockImageRepository.deleteStoredImages() }
        coVerify(exactly = 1) { enrolmentRecordRepository.deleteAll() }
        coVerify(exactly = 1) { eventRepository.deleteAll() }
        coVerify(exactly = 1) { mockSignerManager.signOut() }
    }

    @Test
    fun `when project state is PROJECT_ENDING and there are no events to upload should sign out and delete local data`() =
        runTest {
            val status = SecurityState.Status.PROJECT_ENDING
            val securityState = SecurityState(DEVICE_ID, status)

            securityStateProcessor.processSecurityState(securityState)

            coVerify(exactly = 1) {
                eventSyncManager.countEventsToUpload(projectId = PROJECT_ID, type = null)
            }
            coVerify(exactly = 1) { mockImageRepository.deleteStoredImages() }
            coVerify(exactly = 1) { enrolmentRecordRepository.deleteAll() }
            coVerify(exactly = 1) { eventRepository.deleteAll() }
            coVerify(exactly = 1) { mockSignerManager.signOut() }
        }

    @Test
    fun `when project state is PROJECT_ENDING and there are events to upload should not sign out and not delete local data`() =
        runTest {
            coEvery { eventSyncManager.countEventsToUpload(PROJECT_ID, null) } returns flowOf(1)
            val status = SecurityState.Status.PROJECT_ENDING
            val securityState = SecurityState(
                deviceId = DEVICE_ID,
                status = status,
                mustUpSyncEnrolmentRecords = UpSyncEnrolmentRecords(PROJECT_ID, SUBJECTS)
            )

            securityStateProcessor.processSecurityState(securityState)

            coVerify(exactly = 1) {
                eventSyncManager.countEventsToUpload(projectId = PROJECT_ID, type = null)
            }
            coVerify(exactly = 0) { mockImageRepository.deleteStoredImages() }
            coVerify(exactly = 0) { enrolmentRecordRepository.deleteAll() }
            coVerify(exactly = 0) { eventRepository.deleteAll() }
            coVerify(exactly = 0) { mockSignerManager.signOut() }
            coVerify(exactly = 1) {
                enrolmentRecordScheduler.upload(id = PROJECT_ID, subjectIds = SUBJECTS)
            }
        }

    @Test
    fun withProjectEndedSecurityState_shouldDeleteLocalDataAndSignOut() = runTest {
        val status = SecurityState.Status.PROJECT_ENDED
        val securityState = SecurityState(DEVICE_ID, status)

        securityStateProcessor.processSecurityState(securityState)

        coVerify(exactly = 1) { mockImageRepository.deleteStoredImages() }
        coVerify(exactly = 1) { enrolmentRecordRepository.deleteAll() }
        coVerify(exactly = 1) { eventRepository.deleteAll() }
        coVerify(exactly = 1) { mockSignerManager.signOut() }
    }

    private companion object {
        const val DEVICE_ID = "device-id"
        const val PROJECT_ID = "projectId"
        val SUBJECTS = listOf("subject1")
    }

}
