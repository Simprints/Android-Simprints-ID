package com.simprints.id.services.securitystate

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import com.google.common.truth.Truth.assertThat
import com.simprints.id.testtools.TestApplication
import io.mockk.coEvery
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
        // TODO: replace empty string with SecurityState
        coEvery { worker.repository.getSecurityState() } returns ""
    }

    private fun mockException() {
        coEvery { worker.repository.getSecurityState() } throws Throwable()
    }

}
