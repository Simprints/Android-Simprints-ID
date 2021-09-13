package com.simprints.id.services.securitystate

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.coroutines.DispatcherProvider
import com.simprints.id.secure.models.SecurityState
import com.simprints.id.testtools.TestApplication
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class)
class SecurityStateWorkerTest {

    private val app = ApplicationProvider.getApplicationContext<TestApplication>()

    private lateinit var worker: SecurityStateWorker

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private val testDispatcherProvider = object : DispatcherProvider {
        override fun main(): CoroutineDispatcher = testCoroutineRule.testCoroutineDispatcher

        override fun default(): CoroutineDispatcher = testCoroutineRule.testCoroutineDispatcher

        override fun io(): CoroutineDispatcher = testCoroutineRule.testCoroutineDispatcher

        override fun unconfined(): CoroutineDispatcher = testCoroutineRule.testCoroutineDispatcher
    }

    @Before
    fun setUp() {
        worker = TestListenableWorkerBuilder<SecurityStateWorker>(app).build().apply {
            repository = mockk()
            securityStateProcessor = mockk()
            dispatcher = testDispatcherProvider
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
    fun whenAnExceptionIsThrown_shouldFail() = runBlocking {
        mockException()

        val result = worker.doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.failure())
    }

    private fun mockSuccess() {
        val securityState = SecurityState(DEVICE_ID, SecurityState.Status.RUNNING)
        coEvery { worker.repository.getSecurityState() } returns securityState
    }

    private fun mockException() {
        coEvery { worker.repository.getSecurityState() } throws Throwable()
    }

    private companion object {
        const val DEVICE_ID = "mock-device-id"
    }

}
