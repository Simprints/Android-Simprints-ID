package com.simprints.id.services.securitystate

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import com.google.common.truth.Truth.assertThat
import com.simprints.id.secure.models.SecurityState
import com.simprints.id.testtools.TestApplication
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class)
class SecurityStateWorkerTest {

    private val app = ApplicationProvider.getApplicationContext<TestApplication>()

    private lateinit var worker: SecurityStateWorker

    @Before
    fun setUp() {
        worker = TestListenableWorkerBuilder<SecurityStateWorker>(app).build().apply {
            repository = mockk()
            securityStateProcessor = mockk()
            crashReportManager = mockk(relaxed = true)
        }
        app.component = mockk(relaxed = true)
    }

    @Test
    fun whenSecurityStateIsSuccessfullyFetched_shouldReturnSuccess() = runBlocking {
        mockSuccess()

        val result = worker.doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.success())
    }

    @Test
    fun whenSecurityStateIsSuccessfullyFetched_shouldProcessIt() = runBlocking {
        mockSuccess()

        worker.doWork()

        val expected = SecurityState(DEVICE_ID, SecurityState.Status.RUNNING)
        coVerify { worker.securityStateProcessor.processSecurityState(expected) }
    }

    @Test
    fun whenAnExceptionIsThrown_shouldReturnRetry() = runBlocking {
        mockException()

        val result = worker.doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.retry())
    }

    @Test
    fun whenAnExceptionIsThrown_shouldLogToCrashReport() = runBlocking {
        mockException()

        worker.doWork()

        verify { worker.crashReportManager.logExceptionOrSafeException(any()) }
    }

    private fun mockSuccess() {
        val securityState = SecurityState(DEVICE_ID, SecurityState.Status.RUNNING)
        coEvery { worker.repository.getSecurityStateFromRemote() } returns securityState
    }

    private fun mockException() {
        coEvery { worker.repository.getSecurityStateFromRemote() } throws Throwable()
    }

    private companion object {
        const val DEVICE_ID = "mock-device-id"
    }

}
