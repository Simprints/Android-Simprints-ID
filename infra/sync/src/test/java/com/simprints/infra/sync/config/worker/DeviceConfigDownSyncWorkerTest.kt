package com.simprints.infra.sync.config.worker

import android.os.PowerManager
import androidx.work.ListenableWorker
import com.google.common.truth.Truth.*
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.DeviceState
import com.simprints.infra.config.store.models.SelectDownSyncModules
import com.simprints.infra.config.store.models.UpSyncEnrolmentRecords
import com.simprints.infra.eventsync.module.ModuleSelectionRepository
import com.simprints.infra.sync.OneTime.Action
import com.simprints.infra.sync.OneTime.UpSyncCommand
import com.simprints.infra.sync.OneTime.DownSyncCommand
import com.simprints.infra.sync.SyncOrchestrator
import com.simprints.infra.sync.config.usecase.LogoutUseCase
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.*
import io.mockk.impl.annotations.MockK
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
    private lateinit var syncOrchestrator: SyncOrchestrator

    @MockK
    private lateinit var moduleRepository: ModuleSelectionRepository

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private lateinit var deviceConfigWorker: DeviceConfigDownSyncWorker

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        deviceConfigWorker = DeviceConfigDownSyncWorker(
            context = mockk(relaxed = true) {
                every { getSystemService<PowerManager>(any()) } returns mockk {
                    every { isIgnoringBatteryOptimizations(any()) } returns true
                }
            },
            params = mockk(relaxed = true),
            configRepository = configRepository,
            logoutUseCase = logoutUseCase,
            syncOrchestrator = syncOrchestrator,
            moduleRepository = moduleRepository,
            dispatcher = testCoroutineRule.testCoroutineDispatcher,
        )
    }

    @Test
    fun `Should succeed if not compromised`() = runTest {
        coEvery { configRepository.getDeviceState() } returns DeviceState(
            deviceId = "deviceId",
            isCompromised = false,
        )

        val result = deviceConfigWorker.doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.success())
        coVerify(exactly = 0) { logoutUseCase.invoke() }
        verify(exactly = 0) { syncOrchestrator.uploadEnrolmentRecords(any(), any()) }
    }

    @Test
    fun `Should log out if compromised`() = runTest {
        coEvery { configRepository.getDeviceState() } returns DeviceState(
            deviceId = "deviceId",
            isCompromised = true,
            recordsToUpSync = UpSyncEnrolmentRecords("id", listOf("subjectId")),
        )

        val result = deviceConfigWorker.doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.success())
        coVerify { logoutUseCase.invoke() }
        verify(exactly = 0) { syncOrchestrator.uploadEnrolmentRecords(any(), any()) }
    }

    @Test
    fun `Should schedule record upload if not compromised`() = runTest {
        coEvery { configRepository.getDeviceState() } returns DeviceState(
            deviceId = "deviceId",
            isCompromised = false,
            recordsToUpSync = UpSyncEnrolmentRecords("id", listOf("subjectId")),
        )

        val result = deviceConfigWorker.doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.success())
        coVerify(exactly = 0) { logoutUseCase.invoke() }
        verify { syncOrchestrator.uploadEnrolmentRecords(any(), any()) }
    }

    @Test
    fun `Should update module configuration if present`() = runTest {
        coEvery { configRepository.getDeviceState() } returns DeviceState(
            deviceId = "deviceId",
            isCompromised = false,
            selectModules = SelectDownSyncModules("id", listOf("moduleId".asTokenizableEncrypted())),
        )

        val result = deviceConfigWorker.doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.success())
        coVerify(exactly = 0) { logoutUseCase.invoke() }
        coJustRun { configRepository.updateDeviceConfiguration(any()) }

        coVerify {
            moduleRepository.forceModuleSelection(any(), false)
            configRepository.updateDeviceConfiguration(any())
        }
        verify {
            syncOrchestrator.execute(eq(UpSyncCommand(Action.RESTART)))
            syncOrchestrator.execute(eq(DownSyncCommand(Action.RESTART)))
        }
    }
}
