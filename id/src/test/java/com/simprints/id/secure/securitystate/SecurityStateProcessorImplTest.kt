package com.simprints.id.secure.securitystate

import com.simprints.eventsystem.event.local.EventLocalDataSource
import com.simprints.id.secure.SignerManager
import com.simprints.id.secure.models.SecurityState
import com.simprints.id.secure.models.SecurityState.Status.*
import com.simprints.id.secure.models.UpSyncEnrolmentRecords
import com.simprints.infra.enrolment.records.EnrolmentRecordManager
import com.simprints.infra.images.ImageRepository
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class SecurityStateProcessorImplTest {

    @MockK
    lateinit var mockImageRepository: ImageRepository

    @MockK
    lateinit var mockSignerManager: SignerManager

    @MockK
    lateinit var enrolmentRecordManager: EnrolmentRecordManager

    @MockK
    lateinit var eventLocalDataSource: EventLocalDataSource

    private lateinit var securityStateProcessor: SecurityStateProcessorImpl

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        securityStateProcessor = SecurityStateProcessorImpl(
            mockImageRepository,
            enrolmentRecordManager,
            eventLocalDataSource,
            mockSignerManager,
        )
    }

    @Test
    fun `when there is an instruction it should schedule a work to upload the enrolment records`() =
        runTest {
            val securityState =
                SecurityState(DEVICE_ID, RUNNING, UpSyncEnrolmentRecords("id", listOf("subject1")))

            securityStateProcessor.processSecurityState(securityState)

            coVerify(exactly = 1) { enrolmentRecordManager.upload("id", listOf("subject1")) }
        }

    @Test
    fun withRunningSecurityState_shouldDoNothing() = runTest {
        val status = RUNNING
        val securityState = SecurityState(DEVICE_ID, status)

        securityStateProcessor.processSecurityState(securityState)

        coVerify(exactly = 0) { mockImageRepository.deleteStoredImages() }
        coVerify(exactly = 0) { enrolmentRecordManager.deleteAll() }
        coVerify(exactly = 0) { eventLocalDataSource.deleteAll() }
        coVerify(exactly = 0) { mockSignerManager.signOut() }
    }

    @Test
    fun withCompromisedSecurityState_shouldDeleteLocalDataAndSignOut() = runTest {
        val status = COMPROMISED
        val securityState = SecurityState(DEVICE_ID, status)

        securityStateProcessor.processSecurityState(securityState)

        coVerify(exactly = 1) { mockImageRepository.deleteStoredImages() }
        coVerify(exactly = 1) { enrolmentRecordManager.deleteAll() }
        coVerify(exactly = 1) { eventLocalDataSource.deleteAll() }
        coVerify(exactly = 1) { mockSignerManager.signOut() }
    }

    @Test
    fun withProjectEndedSecurityState_shouldDeleteLocalDataAndSignOut() = runTest {
        val status = PROJECT_ENDED
        val securityState = SecurityState(DEVICE_ID, status)

        securityStateProcessor.processSecurityState(securityState)

        coVerify(exactly = 1) { mockImageRepository.deleteStoredImages() }
        coVerify(exactly = 1) { enrolmentRecordManager.deleteAll() }
        coVerify(exactly = 1) { eventLocalDataSource.deleteAll() }
        coVerify(exactly = 1) { mockSignerManager.signOut() }
    }

    private companion object {
        const val DEVICE_ID = "device-id"
    }

}
