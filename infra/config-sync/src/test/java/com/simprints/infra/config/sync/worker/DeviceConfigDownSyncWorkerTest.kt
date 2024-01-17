package com.simprints.infra.config.sync.worker

import androidx.work.ListenableWorker
import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.DeviceState
import com.simprints.infra.config.store.models.UpSyncEnrolmentRecords
import com.simprints.infra.config.sync.usecase.LogoutUseCase
import com.simprints.infra.enrolment.records.sync.worker.EnrolmentRecordScheduler
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class DeviceConfigDownSyncWorkerTest {

    @MockK
    private lateinit var configRepository: ConfigRepository

    @MockK
    private lateinit var logoutUseCase: LogoutUseCase

    @MockK
    private lateinit var enrolmentRecordScheduler: EnrolmentRecordScheduler

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private lateinit var deviceConfigWorker: DeviceConfigDownSyncWorker

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        deviceConfigWorker = DeviceConfigDownSyncWorker(
            context = mockk(),
            params = mockk(relaxed = true),
            configRepository = configRepository,
            logoutUseCase = logoutUseCase,
            enrolmentRecordScheduler = enrolmentRecordScheduler,
            dispatcher = testCoroutineRule.testCoroutineDispatcher,
        )
    }

    @Test
    fun `Should succeed if not compromised`() = runTest {
        coEvery { configRepository.getDeviceState() } returns DeviceState(
            "deviceId",
            false,
        )

        val result = deviceConfigWorker.doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.success())
        coVerify(exactly = 0) { logoutUseCase.invoke() }
        verify(exactly = 0) { enrolmentRecordScheduler.upload(any(), any()) }
    }

    @Test
    fun `Should log out if compromised`() = runTest {
        coEvery { configRepository.getDeviceState() } returns DeviceState(
            "deviceId",
            true,
            UpSyncEnrolmentRecords("id", listOf("subjectId"))
        )

        val result = deviceConfigWorker.doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.success())
        coVerify { logoutUseCase.invoke() }
        verify(exactly = 0) { enrolmentRecordScheduler.upload(any(), any()) }
    }

    @Test
    fun `Should schedule record upload if not compromised`() = runTest {
        coEvery { configRepository.getDeviceState() } returns DeviceState(
            "deviceId",
            false,
            UpSyncEnrolmentRecords("id", listOf("subjectId"))
        )

        val result = deviceConfigWorker.doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.success())
        coVerify(exactly = 0) { logoutUseCase.invoke() }
        verify { enrolmentRecordScheduler.upload(any(), any()) }
    }
}
