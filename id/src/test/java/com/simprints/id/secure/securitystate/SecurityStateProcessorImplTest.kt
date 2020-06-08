package com.simprints.id.secure.securitystate

import com.simprints.id.data.db.person.PersonRepository
import com.simprints.id.data.db.session.SessionRepository
import com.simprints.id.data.images.repository.ImageRepository
import com.simprints.id.secure.SignerManager
import com.simprints.id.secure.models.SecurityState
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class SecurityStateProcessorImplTest {

    @MockK lateinit var mockImageRepository: ImageRepository
    @MockK lateinit var mockPersonRepository: PersonRepository
    @MockK lateinit var mockSessionRepository: SessionRepository
    @MockK lateinit var mockSignerManager: SignerManager

    private lateinit var securityStateProcessor: SecurityStateProcessorImpl

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        securityStateProcessor = SecurityStateProcessorImpl(
            mockImageRepository,
            mockPersonRepository,
            mockSessionRepository,
            mockSignerManager
        )
    }

    @Test
    fun withRunningSecurityState_shouldDoNothing() {
        val status = SecurityState.Status.RUNNING
        val securityState = SecurityState(DEVICE_ID, status)

        runBlocking {
            securityStateProcessor.processSecurityState(securityState)
        }

        verify(exactly = 0) { mockImageRepository.deleteStoredImages() }
        coVerify(exactly = 0) { mockPersonRepository.deleteAll() }
        coVerify(exactly = 0) { mockSessionRepository.deleteAllFromLocal() }
        coVerify(exactly = 0) { mockSignerManager.signOut() }
    }

    @Test
    fun withCompromisedSecurityState_shouldDeleteLocalDataAndSignOut() {
        val status = SecurityState.Status.COMPROMISED
        val securityState = SecurityState(DEVICE_ID, status)

        runBlocking {
            securityStateProcessor.processSecurityState(securityState)
        }

        verify(exactly = 1) { mockImageRepository.deleteStoredImages() }
        coVerify(exactly = 1) { mockPersonRepository.deleteAll() }
        coVerify(exactly = 1) { mockSessionRepository.deleteAllFromLocal() }
        coVerify(exactly = 1) { mockSignerManager.signOut() }
    }

    @Test
    fun withProjectEndedSecurityState_shouldDeleteLocalDataAndSignOut() {
        val status = SecurityState.Status.PROJECT_ENDED
        val securityState = SecurityState(DEVICE_ID, status)

        runBlocking {
            securityStateProcessor.processSecurityState(securityState)
        }

        verify(exactly = 1) { mockImageRepository.deleteStoredImages() }
        coVerify(exactly = 1) { mockPersonRepository.deleteAll() }
        coVerify(exactly = 1) { mockSessionRepository.deleteAllFromLocal() }
        coVerify(exactly = 1) { mockSignerManager.signOut() }
    }

    private companion object {
        const val DEVICE_ID = "device-id"
    }

}
