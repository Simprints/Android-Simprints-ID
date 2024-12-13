package com.simprints.fingerprint.connect.usecase

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.time.TimeHelper
import com.simprints.fingerprint.infra.scanner.ScannerManager
import com.simprints.fingerprint.infra.scanner.domain.BatteryInfo
import com.simprints.fingerprint.infra.scanner.domain.ScannerGeneration
import com.simprints.fingerprint.infra.scanner.domain.versions.ScannerFirmwareVersions
import com.simprints.infra.events.event.domain.models.ScannerConnectionEvent
import com.simprints.infra.events.event.domain.models.Vero2InfoSnapshotEvent
import com.simprints.infra.events.session.SessionEventRepository
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SaveScannerConnectionEventsUseCaseTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    lateinit var scannerManager: ScannerManager

    @MockK
    lateinit var timeHelper: TimeHelper

    @MockK
    lateinit var eventRepository: SessionEventRepository

    private lateinit var useCase: SaveScannerConnectionEventsUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        useCase = SaveScannerConnectionEventsUseCase(
            scannerManager,
            timeHelper,
            eventRepository,
            CoroutineScope(testCoroutineRule.testCoroutineDispatcher),
        )
    }

    @Test
    fun `Saves only connection event for Vero 1`() {
        every { scannerManager.currentScannerId } returns "id"
        every { scannerManager.currentMacAddress } returns "mac"

        every { scannerManager.scanner } returns mockk {
            every { versionInformation().generation } returns ScannerGeneration.VERO_1
            every { versionInformation().firmware.stm } returns "smt"
        }

        useCase.invoke()

        coVerify(exactly = 1) {
            eventRepository.addOrUpdateEvent(
                withArg<ScannerConnectionEvent> {
                    assertThat(it.payload.scannerInfo.scannerId).isEqualTo("id")
                    assertThat(it.payload.scannerInfo.macAddress).isEqualTo("mac")
                    assertThat(it.payload.scannerInfo.hardwareVersion).isEqualTo("smt")
                },
            )
        }
    }

    @Test
    fun `Saves connection and info events for Vero 2`() {
        every { scannerManager.scanner } returns mockk {
            every { versionInformation().generation } returns ScannerGeneration.VERO_2
            every { versionInformation().hardwareVersion } returns "v2"
            every { versionInformation().firmware } returns ScannerFirmwareVersions("", "", "")
            every { batteryInformation() } returns BatteryInfo(0, 0, 0, 0)
        }

        useCase.invoke()

        coVerify {
            eventRepository.addOrUpdateEvent(
                withArg<ScannerConnectionEvent> {
                    assertThat(it.payload.scannerInfo.hardwareVersion).isEqualTo("v2")
                },
            )

            eventRepository.addOrUpdateEvent(withArg<Vero2InfoSnapshotEvent> {})
        }
    }
}
