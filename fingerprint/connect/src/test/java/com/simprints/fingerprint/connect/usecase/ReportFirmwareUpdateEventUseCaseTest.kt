package com.simprints.fingerprint.connect.usecase

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.fingerprint.infra.scanner.domain.ota.AvailableOta
import com.simprints.infra.events.event.domain.models.ScannerFirmwareUpdateEvent
import com.simprints.infra.events.session.SessionEventRepository
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ReportFirmwareUpdateEventUseCaseTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    lateinit var timeHelper: TimeHelper

    @MockK
    lateinit var eventRepository: SessionEventRepository

    private lateinit var useCase: ReportFirmwareUpdateEventUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        every { timeHelper.now() } returns Timestamp(1L)

        useCase = ReportFirmwareUpdateEventUseCase(
            timeHelper,
            eventRepository,
            CoroutineScope(testCoroutineRule.testCoroutineDispatcher),
        )
    }

    @Test
    fun `Correctly maps cypress chip name`() = runTest {
        useCase(Timestamp(0L), AvailableOta.CYPRESS, "v1")

        coVerify {
            eventRepository.addOrUpdateEvent(
                withArg<ScannerFirmwareUpdateEvent> {
                    assertThat(it.payload.chip).isEqualTo("cypress")
                },
            )
        }
    }

    @Test
    fun `Correctly maps stm chip name`() = runTest {
        useCase(Timestamp(0L), AvailableOta.STM, "v1")

        coVerify {
            eventRepository.addOrUpdateEvent(
                withArg<ScannerFirmwareUpdateEvent> {
                    assertThat(it.payload.chip).isEqualTo("stm")
                },
            )
        }
    }

    @Test
    fun `Correctly maps UN20 chip name`() = runTest {
        useCase(Timestamp(0L), AvailableOta.UN20, "v1")

        coVerify {
            eventRepository.addOrUpdateEvent(
                withArg<ScannerFirmwareUpdateEvent> {
                    assertThat(it.payload.chip).isEqualTo("un20")
                },
            )
        }
    }

    @Test
    fun `Correctly maps exception`() = runTest {
        useCase(Timestamp(0L), AvailableOta.UN20, "v1", RuntimeException("message"))

        coVerify {
            eventRepository.addOrUpdateEvent(
                withArg<ScannerFirmwareUpdateEvent> {
                    assertThat(it.payload.failureReason).isEqualTo("RuntimeException : message")
                },
            )
        }
    }
}
