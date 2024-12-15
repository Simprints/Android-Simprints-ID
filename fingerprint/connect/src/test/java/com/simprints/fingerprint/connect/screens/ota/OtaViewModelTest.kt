package com.simprints.fingerprint.connect.screens.ota

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.fingerprint.connect.usecase.ReportFirmwareUpdateEventUseCase
import com.simprints.fingerprint.infra.scanner.ScannerManager
import com.simprints.fingerprint.infra.scanner.domain.ota.AvailableOta
import com.simprints.fingerprint.infra.scanner.domain.ota.CypressOtaStep
import com.simprints.fingerprint.infra.scanner.domain.ota.OtaRecoveryStrategy
import com.simprints.fingerprint.infra.scanner.domain.ota.StmOtaStep
import com.simprints.fingerprint.infra.scanner.domain.ota.Un20OtaStep
import com.simprints.fingerprint.infra.scanner.exceptions.safe.OtaFailedException
import com.simprints.fingerprint.infra.scanner.wrapper.ScannerOtaOperationsWrapper
import com.simprints.infra.config.store.models.FingerprintConfiguration.BioSdk.SECUGEN_SIM_MATCHER
import com.simprints.infra.config.store.models.Vero2Configuration
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.network.exceptions.BackendMaintenanceException
import com.simprints.infra.recent.user.activity.RecentUserActivityManager
import com.simprints.infra.recent.user.activity.domain.RecentUserActivity
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.livedata.assertEventReceived
import com.simprints.testtools.common.livedata.assertEventReceivedWithContent
import com.simprints.testtools.common.livedata.assertEventReceivedWithContentAssertions
import com.simprints.testtools.common.livedata.testObserver
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.justRun
import io.mockk.verify
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

class OtaViewModelTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    private lateinit var reportFirmwareUpdate: ReportFirmwareUpdateEventUseCase

    @MockK
    private lateinit var timeHelperMock: TimeHelper

    @MockK
    private lateinit var scannerOtaWrapper: ScannerOtaOperationsWrapper

    @MockK
    private lateinit var scannerManager: ScannerManager

    @MockK
    private lateinit var recentUserActivityManager: RecentUserActivityManager

    @MockK
    private lateinit var configManager: ConfigManager

    private lateinit var otaViewModel: OtaViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        every { timeHelperMock.now() } returns Timestamp(1L)
        coEvery {
            recentUserActivityManager.getRecentUserActivity()
        } returns RecentUserActivity(HARDWARE_VERSION, "", "".asTokenizableRaw(), 0, 0, 0, 0)
        coEvery {
            configManager
                .getProjectConfiguration()
                .fingerprint
                ?.getSdkConfiguration(
                    SECUGEN_SIM_MATCHER,
                )?.vero2
                ?.firmwareVersions
        } returns
            mapOf(
                HARDWARE_VERSION to Vero2Configuration.Vero2FirmwareVersions(
                    NEW_CYPRESS_VERSION,
                    NEW_STM_VERSION,
                    NEW_UN20_VERSION,
                ),
            )

        every { scannerOtaWrapper.performCypressOta(any()) } returns CYPRESS_OTA_STEPS.asFlow()
        every { scannerOtaWrapper.performStmOta(any()) } returns STM_OTA_STEPS.asFlow()
        every { scannerOtaWrapper.performUn20Ota(any()) } returns UN20_OTA_STEPS.asFlow()
        every { scannerManager.otaOperationsWrapper } returns scannerOtaWrapper

        otaViewModel = OtaViewModel(
            scannerManager,
            reportFirmwareUpdate,
            timeHelperMock,
            recentUserActivityManager,
            configManager,
        )
    }

    @Test
    fun oneOta_updatesStateCorrectlyAndSavesEvent() = runTest {
        val progressObserver = otaViewModel.progress.testObserver()

        otaViewModel.startOta(SECUGEN_SIM_MATCHER, listOf(AvailableOta.CYPRESS), 0)

        progressObserver.observedValues.assertAlmostContainsExactlyElementsIn(
            listOf(0f) + CYPRESS_OTA_STEPS.map { it.totalProgress } + listOf(1f),
        )
        otaViewModel.otaComplete.assertEventReceived()

        verify { reportFirmwareUpdate.invoke(any(), any(), any()) }
    }

    @Test
    fun multipleOtas_updatesStateCorrectlyAndSavesEvents() = runTest {
        val capturedOtas = mutableListOf<AvailableOta>()
        justRun { reportFirmwareUpdate.invoke(any(), capture(capturedOtas), any()) }

        val progressObserver = otaViewModel.progress.testObserver()
        val completeObserver = otaViewModel.otaComplete.testObserver()

        otaViewModel.startOta(SECUGEN_SIM_MATCHER, listOf(AvailableOta.CYPRESS, AvailableOta.STM, AvailableOta.UN20), 0)

        progressObserver.observedValues.assertAlmostContainsExactlyElementsIn(
            listOf(0f) +
                CYPRESS_OTA_STEPS.map { it.totalProgress / 3f } +
                STM_OTA_STEPS.map { (it.totalProgress + 1f) / 3f } +
                UN20_OTA_STEPS.map { (it.totalProgress + 2f) / 3f } +
                listOf(1f),
        )
        completeObserver.assertEventReceived()

        assertThat(capturedOtas.size).isEqualTo(3)
        assertThat(capturedOtas[0]).isEqualTo(AvailableOta.CYPRESS)
        assertThat(capturedOtas[1]).isEqualTo(AvailableOta.STM)
        assertThat(capturedOtas[2]).isEqualTo(AvailableOta.UN20)
    }

    @Test
    fun otaFailsFirstAttempt_otaException_correctlyUpdatesStateAndSavesEvent() = runTest {
        justRun { reportFirmwareUpdate.invoke(any(), any(), any(), any()) }
        coEvery { scannerOtaWrapper.performStmOta(any()) } returns flow {
            emit(STM_OTA_STEPS[0])
            throw OtaFailedException("oops")
        }

        otaViewModel.startOta(SECUGEN_SIM_MATCHER, listOf(AvailableOta.CYPRESS, AvailableOta.STM, AvailableOta.UN20), 0)

        verify { reportFirmwareUpdate.invoke(any(), any(), any(), null) }
        verify { reportFirmwareUpdate.invoke(any(), any(), any(), any()) }

        otaViewModel.otaRecovery.assertEventReceivedWithContentAssertions {
            assertThat(it?.recoveryStrategy).isEqualTo(OtaRecoveryStrategy.HARD_RESET)
            assertThat(it?.remainingOtas).isEqualTo(listOf(AvailableOta.STM, AvailableOta.UN20))
        }
    }

    @Test
    fun otaFailsLastAttempt_otaException_correctlyUpdatesStateAndSavesEvent() = runTest {
        justRun { reportFirmwareUpdate.invoke(any(), any(), any(), any()) }
        coEvery { scannerOtaWrapper.performStmOta(any()) } returns flow {
            emit(STM_OTA_STEPS[0])
            throw OtaFailedException("oops")
        }

        otaViewModel.startOta(
            SECUGEN_SIM_MATCHER,
            listOf(AvailableOta.CYPRESS, AvailableOta.STM, AvailableOta.UN20),
            OtaViewModel.MAX_RETRY_ATTEMPTS,
        )

        verify(exactly = 2) { reportFirmwareUpdate.invoke(any(), any(), any(), any()) }

        otaViewModel.otaFailed.assertEventReceivedWithContent(FetchOtaResult())
    }

    @Test
    fun otaFailsLastAttempt_backendMaintenanceException_correctlyUpdatesStateAndSavesEvent() = runTest {
        val capturedOtas = mutableListOf<AvailableOta>()
        justRun { reportFirmwareUpdate.invoke(any(), capture(capturedOtas), any(), any()) }
        coEvery { scannerOtaWrapper.performStmOta(any()) } returns flow {
            emit(STM_OTA_STEPS[0])
            throw BackendMaintenanceException(estimatedOutage = null)
        }

        otaViewModel.startOta(
            SECUGEN_SIM_MATCHER,
            listOf(AvailableOta.CYPRESS, AvailableOta.STM, AvailableOta.UN20),
            OtaViewModel.MAX_RETRY_ATTEMPTS,
        )

        verify { reportFirmwareUpdate.invoke(any(), any(), any(), any()) }
        assertThat(capturedOtas[1]).isEqualTo(AvailableOta.STM)

        otaViewModel.otaFailed.assertEventReceivedWithContent(FetchOtaResult(isMaintenanceMode = true))
    }

    @Test
    fun otaFailsFirstAttempt_otaExceptionRequiringDelay_correctlyDelaysThenUpdatesStateAndSavesEvent() = runTest {
        justRun { reportFirmwareUpdate.invoke(any(), any(), any(), any()) }
        coEvery { scannerOtaWrapper.performUn20Ota(any()) } returns flow {
            emit(Un20OtaStep.AwaitingCacheCommit)
            throw OtaFailedException("oops")
        }

        otaViewModel.startOta(
            SECUGEN_SIM_MATCHER,
            listOf(AvailableOta.CYPRESS, AvailableOta.STM, AvailableOta.UN20),
            0,
        )

        testCoroutineRule.testCoroutineDispatcher.scheduler.advanceTimeBy(11.seconds)

        verify(exactly = 3) { reportFirmwareUpdate.invoke(any(), any(), any(), any()) }

        otaViewModel.otaRecovery.assertEventReceivedWithContentAssertions {
            assertThat(it?.recoveryStrategy).isEqualTo(OtaRecoveryStrategy.SOFT_RESET)
            assertThat(it?.remainingOtas).isEqualTo(listOf(AvailableOta.UN20))
        }
    }

    private fun Iterable<Float?>.assertAlmostContainsExactlyElementsIn(
        expected: Iterable<Float>,
        threshold: Float = 1e-5f,
    ) {
        this.zip(expected).forEach { (a, b) -> assertThat(a).isWithin(threshold).of(b) }
    }

    companion object {
        private const val HARDWARE_VERSION = "E-1"

        private const val NEW_CYPRESS_VERSION = "5.E-1.1"
        private const val NEW_STM_VERSION = "4.E-1.7"
        private const val NEW_UN20_VERSION = "8.E-1.0"

        private val OTA_PROGRESS_VALUES = listOf(0.0f, 0.2f, 0.4f, 0.6f, 0.8f, 1.0f)

        private val CYPRESS_OTA_STEPS =
            listOf(CypressOtaStep.EnteringOtaMode, CypressOtaStep.CommencingTransfer) +
                OTA_PROGRESS_VALUES.map { CypressOtaStep.TransferInProgress(it) } +
                listOf(
                    CypressOtaStep.ReconnectingAfterTransfer,
                    CypressOtaStep.ValidatingNewFirmwareVersion,
                    CypressOtaStep.UpdatingUnifiedVersionInformation,
                )

        private val STM_OTA_STEPS = listOf(
            StmOtaStep.EnteringOtaModeFirstTime,
            StmOtaStep.ReconnectingAfterEnteringOtaMode,
            StmOtaStep.EnteringOtaModeSecondTime,
            StmOtaStep.CommencingTransfer,
        ) +
            OTA_PROGRESS_VALUES.map { StmOtaStep.TransferInProgress(it) } +
            listOf(
                StmOtaStep.ReconnectingAfterTransfer,
                StmOtaStep.EnteringMainMode,
                StmOtaStep.ValidatingNewFirmwareVersion,
                StmOtaStep.ReconnectingAfterValidating,
                StmOtaStep.UpdatingUnifiedVersionInformation,
            )

        private val UN20_OTA_STEPS =
            listOf(
                Un20OtaStep.EnteringMainMode,
                Un20OtaStep.TurningOnUn20BeforeTransfer,
                Un20OtaStep.CommencingTransfer,
            ) +
                OTA_PROGRESS_VALUES.map { Un20OtaStep.TransferInProgress(it) } +
                listOf(
                    Un20OtaStep.AwaitingCacheCommit,
                    Un20OtaStep.TurningOffUn20AfterTransfer,
                    Un20OtaStep.TurningOnUn20AfterTransfer,
                    Un20OtaStep.ValidatingNewFirmwareVersion,
                    Un20OtaStep.ReconnectingAfterValidating,
                    Un20OtaStep.UpdatingUnifiedVersionInformation,
                )
    }
}
