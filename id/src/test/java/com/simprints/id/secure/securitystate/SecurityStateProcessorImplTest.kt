package com.simprints.id.secure.securitystate

import com.simprints.id.data.db.subject.SubjectRepository
import com.simprints.id.data.images.repository.ImageRepository
import com.simprints.id.enrolmentrecords.worker.EnrolmentRecordScheduler
import com.simprints.id.secure.SignerManager
import com.simprints.id.secure.models.SecurityState
import com.simprints.id.secure.models.SecurityState.Status.*
import com.simprints.id.secure.models.SyncEnrolmentRecord
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class SecurityStateProcessorImplTest {

    @MockK
    lateinit var mockImageRepository: ImageRepository

    @MockK
    lateinit var mockSubjectRepository: SubjectRepository

    @MockK
    lateinit var mockSignerManager: SignerManager

    @MockK
    lateinit var enrolmentRecordScheduler: EnrolmentRecordScheduler

    private lateinit var securityStateProcessor: SecurityStateProcessorImpl

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        securityStateProcessor = SecurityStateProcessorImpl(
            mockImageRepository,
            mockSubjectRepository,
            enrolmentRecordScheduler,
            mockSignerManager
        )
    }

    @Test
    fun `when there is an instruction it should schedule a work to upload the enrolment records`() =
        runTest {
            val securityState =
                SecurityState(DEVICE_ID, RUNNING, SyncEnrolmentRecord("id", listOf("subject1")))

            securityStateProcessor.processSecurityState(securityState)

            coVerify(exactly = 1) { enrolmentRecordScheduler.upload("id", listOf("subject1")) }
        }

    @Test
    fun withRunningSecurityState_shouldDoNothing() = runTest {
        val status = RUNNING
        val securityState = SecurityState(DEVICE_ID, status)

        securityStateProcessor.processSecurityState(securityState)

        verify(exactly = 0) { mockImageRepository.deleteStoredImages() }
        coVerify(exactly = 0) { mockSubjectRepository.deleteAll() }
        coVerify(exactly = 0) { mockSignerManager.signOut() }
    }

    @Test
    fun withCompromisedSecurityState_shouldDeleteLocalDataAndSignOut() = runTest {
        val status = COMPROMISED
        val securityState = SecurityState(DEVICE_ID, status)

        securityStateProcessor.processSecurityState(securityState)

        verify(exactly = 1) { mockImageRepository.deleteStoredImages() }
        coVerify(exactly = 1) { mockSubjectRepository.deleteAll() }
        coVerify(exactly = 1) { mockSignerManager.signOut() }
    }

    @Test
    fun withProjectEndedSecurityState_shouldDeleteLocalDataAndSignOut() = runTest {
        val status = PROJECT_ENDED
        val securityState = SecurityState(DEVICE_ID, status)

        securityStateProcessor.processSecurityState(securityState)

        verify(exactly = 1) { mockImageRepository.deleteStoredImages() }
        coVerify(exactly = 1) { mockSubjectRepository.deleteAll() }
        coVerify(exactly = 1) { mockSignerManager.signOut() }
    }

    private companion object {
        const val DEVICE_ID = "device-id"
    }

}
