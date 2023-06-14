package com.simprints.infra.authlogic

import com.simprints.infra.authlogic.authenticator.Authenticator
import com.simprints.infra.authlogic.authenticator.SignerManager
import com.simprints.infra.authlogic.worker.SecurityStateScheduler
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test


internal class AuthManagerImplTest {
    @MockK
    lateinit var authenticator: Authenticator

    @MockK
    lateinit var securityStateScheduler: SecurityStateScheduler

    @MockK
    lateinit var signerManager: SignerManager

    private lateinit var authManager: AuthManagerImpl

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        authManager = AuthManagerImpl(
            authenticator = authenticator,
            securityStateScheduler = securityStateScheduler,
            signerManager = signerManager
        )
    }

    @Test
    fun `should call authenticator when authenticateSafely is called`() {
        val userId = "userId"
        val projectId = "projectId"
        val projectSecret = "projectSecret"
        val deviceId = "deviceId"
        runTest {
            authManager.authenticateSafely(
                userId = userId,
                projectId = projectId,
                projectSecret = projectSecret,
                deviceId = deviceId
            )
            coVerify(exactly = 1) {
                authenticator.authenticate(
                    userId = userId,
                    projectId = projectId,
                    projectSecret = projectSecret,
                    deviceId = deviceId
                )
            }
        }
    }

    @Test
    fun `should call securityStateScheduler when startSecurityStateCheck is called`() {
        authManager.startSecurityStateCheck()
        verify(exactly = 1) { securityStateScheduler.startSecurityStateCheck() }
    }

    @Test
    fun `should call signerManager when signOut is called`() {
        runTest {
            authManager.signOut()
            coVerify(exactly = 1) { signerManager.signOut() }
        }
    }

}