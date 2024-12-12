package com.simprints.fingerprint.capture.usecase

import com.simprints.core.domain.fingerprint.IFingerIdentifier
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.core.tools.utils.EncodingUtils
import com.simprints.fingerprint.capture.state.CaptureState
import com.simprints.fingerprint.capture.state.FingerState
import com.simprints.fingerprint.capture.state.ScanResult
import com.simprints.infra.events.event.domain.models.fingerprint.FingerprintCaptureBiometricsEvent
import com.simprints.infra.events.event.domain.models.fingerprint.FingerprintCaptureEvent
import com.simprints.infra.events.session.SessionEventRepository
import io.mockk.MockKAnnotations
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

internal class AddCaptureEventsUseCaseTest {
    @MockK
    lateinit var timeHelper: TimeHelper

    @MockK
    lateinit var encoder: EncodingUtils

    @MockK
    lateinit var eventRepo: SessionEventRepository

    private lateinit var useCase: AddCaptureEventsUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        coJustRun { eventRepo.addOrUpdateEvent(any()) }

        useCase = AddCaptureEventsUseCase(timeHelper, encoder, eventRepo)
    }

    @Test
    fun `Saves only capture event when not collected state`() = runTest {
        useCase.invoke(
            Timestamp(1L),
            FingerState(IFingerIdentifier.LEFT_THUMB, listOf(CaptureState.NotCollected)),
            10,
            false,
        )

        coVerify { eventRepo.addOrUpdateEvent(withArg<FingerprintCaptureEvent> {}) }
        coVerify(exactly = 0) { eventRepo.addOrUpdateEvent(withArg<FingerprintCaptureBiometricsEvent> {}) }
    }

    @Test
    fun `Saves only capture event when not a good scan and not too many bad scans`() = runTest {
        useCase.invoke(
            Timestamp(1L),
            FingerState(
                IFingerIdentifier.LEFT_THUMB,
                listOf(
                    CaptureState.ScanProcess.Collected(
                        numberOfBadScans = 0,
                        numberOfNoFingerDetectedScans = 0,
                        scanResult = ScanResult(0, byteArrayOf(), "", null, 10),
                    ),
                ),
            ),
            10,
            false,
        )

        coVerify { eventRepo.addOrUpdateEvent(withArg<FingerprintCaptureEvent> {}) }
        coVerify(exactly = 0) { eventRepo.addOrUpdateEvent(withArg<FingerprintCaptureBiometricsEvent> {}) }
    }

    @Test
    fun `Saves biometric event when good scan`() = runTest {
        useCase.invoke(
            Timestamp(1L),
            FingerState(
                IFingerIdentifier.LEFT_THUMB,
                listOf(
                    CaptureState.ScanProcess.Collected(
                        numberOfBadScans = 0,
                        numberOfNoFingerDetectedScans = 0,
                        scanResult = ScanResult(100, byteArrayOf(), "", null, 10),
                    ),
                ),
            ),
            10,
            false,
        )

        coVerify {
            eventRepo.addOrUpdateEvent(withArg<FingerprintCaptureEvent> {})
            eventRepo.addOrUpdateEvent(withArg<FingerprintCaptureBiometricsEvent> {})
        }
    }

    @Test
    fun `Saves biometric event when too many bad scans`() = runTest {
        useCase.invoke(
            Timestamp(1L),
            FingerState(
                IFingerIdentifier.LEFT_THUMB,
                listOf(
                    CaptureState.ScanProcess.Collected(
                        numberOfBadScans = 0,
                        numberOfNoFingerDetectedScans = 0,
                        scanResult = ScanResult(0, byteArrayOf(), "", null, 10),
                    ),
                ),
            ),
            10,
            true,
        )

        coVerify {
            eventRepo.addOrUpdateEvent(withArg<FingerprintCaptureEvent> {})
            eventRepo.addOrUpdateEvent(withArg<FingerprintCaptureBiometricsEvent> {})
        }
    }
}
