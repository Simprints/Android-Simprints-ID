package com.simprints.id.services.securitystate

import androidx.work.ListenableWorker
import com.google.common.truth.Truth.assertThat
import com.simprints.id.secure.models.SecurityState
import com.simprints.id.secure.securitystate.SecurityStateProcessor
import com.simprints.id.secure.securitystate.repository.SecurityStateRepository
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class SecurityStateWorkerTest {

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private val securityStateRepository = mockk<SecurityStateRepository>()
    private val securityStateProcessor = mockk<SecurityStateProcessor>(relaxed = true)

    private val worker = SecurityStateWorker(
        mockk(relaxed = true),
        mockk(relaxed = true),
        securityStateRepository,
        securityStateProcessor,
        testCoroutineRule.testCoroutineDispatcher
    )

    @Test
    fun whenSecurityStateIsSuccessfullyFetched_shouldReturnSuccess() = runTest {
        val securityState = SecurityState("mock-device-id", SecurityState.Status.RUNNING)
        coEvery { securityStateRepository.getSecurityStatusFromRemote() } returns securityState

        val result = worker.doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.success())
        coVerify { securityStateProcessor.processSecurityState(securityState) }
    }

    @Test
    fun whenAnExceptionIsThrown_shouldFail() = runTest {
        coEvery { securityStateRepository.getSecurityStatusFromRemote() } throws Throwable()

        val result = worker.doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.failure())
    }
}
