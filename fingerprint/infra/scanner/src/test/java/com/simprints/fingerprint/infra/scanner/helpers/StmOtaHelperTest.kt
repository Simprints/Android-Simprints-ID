package com.simprints.fingerprint.infra.scanner.helpers

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.infra.scanner.data.local.FirmwareLocalDataSource
import com.simprints.fingerprint.infra.scanner.domain.ota.StmOtaStep
import com.simprints.fingerprint.infra.scanner.exceptions.safe.OtaFailedException
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.Un20ExtendedAppVersion
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.models.StmExtendedFirmwareVersion
import com.simprints.fingerprint.infra.scanner.v2.domain.root.models.CypressExtendedFirmwareVersion
import com.simprints.fingerprint.infra.scanner.v2.domain.root.models.ExtendedVersionInformation
import com.simprints.fingerprint.infra.scanner.v2.domain.root.models.ScannerInformation
import com.simprints.fingerprint.infra.scanner.v2.scanner.Scanner
import io.mockk.CapturingSlot
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.io.IOException
import com.simprints.fingerprint.infra.scanner.v2.exceptions.ota.OtaFailedException as ScannerV2OtaFailedException

class StmOtaHelperTest {
    private val scannerMock = mockk<Scanner>()
    private val connectionHelperMock = mockk<ConnectionHelper>()
    private val firmwareFileManagerMock = mockk<FirmwareLocalDataSource>()
    private val stmOtaHelper = StmOtaHelper(connectionHelperMock, firmwareFileManagerMock)

    @Before
    fun setup() {
        coEvery { connectionHelperMock.reconnect(any(), any()) } answers {}

        coEvery { scannerMock.enterStmOtaMode() } just runs
        coEvery { scannerMock.startStmOta(any()) } returns OTA_PROGRESS_VALUES.asFlow()
        coEvery { scannerMock.getVersionInformation() } returns OLD_SCANNER_VERSION
        coEvery { scannerMock.setVersionInformation(any()) } just runs
        coEvery { scannerMock.enterMainMode() } just runs
        coEvery { scannerMock.getStmFirmwareVersion() } returns NEW_STM_VERSION

        coEvery { firmwareFileManagerMock.loadStmFirmwareBytes(NEW_STM_VERSION_STRING) } returns
            byteArrayOf(0x00, 0x01, 0x02, 0xFF.toByte())
    }

    @Test
    fun performStmOta_allStepsPassing_succeedsWithCorrectStepsAndProgressValues() = runTest {
        val expectedSteps = listOf(
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

        val actualSteps = stmOtaHelper.performOtaSteps(scannerMock, "mac address", NEW_STM_VERSION_STRING).toList()

        assertThat(actualSteps).containsExactlyElementsIn(expectedSteps).inOrder()
        assertThat(actualSteps.map { it.totalProgress })
            .containsExactlyElementsIn(expectedSteps.map { it.totalProgress })
            .inOrder()

        val sentUnifiedVersion = CapturingSlot<ExtendedVersionInformation>()
        coVerify { scannerMock.setVersionInformation(capture(sentUnifiedVersion)) }
        assertThat(sentUnifiedVersion.captured.toScannerFirmwareVersions()).isEqualTo(NEW_SCANNER_VERSION.toScannerFirmwareVersions())
    }

    @Test(expected = ScannerV2OtaFailedException::class)
    fun stmOtaFailsDuringTransfer_propagatesError() = runTest {
        val progressValues = listOf(0.0f, 0.2f, 0.4f)
        val expectedSteps = listOf(
            StmOtaStep.EnteringOtaModeFirstTime,
            StmOtaStep.ReconnectingAfterEnteringOtaMode,
            StmOtaStep.EnteringOtaModeSecondTime,
            StmOtaStep.CommencingTransfer,
        ) +
            progressValues.map { StmOtaStep.TransferInProgress(it) }
        val error = ScannerV2OtaFailedException("oops!")

        coEvery { scannerMock.startStmOta(any()) } returns progressValues.asFlow().onCompletion { throw error }

        val otaFlow = stmOtaHelper.performOtaSteps(scannerMock, "mac address", NEW_STM_VERSION_STRING)
        val actualSteps = otaFlow.take(expectedSteps.size).toList()

        assertThat(actualSteps).containsExactlyElementsIn(expectedSteps).inOrder()
        assertThat(actualSteps.map { it.totalProgress })
            .containsExactlyElementsIn(expectedSteps.map { it.totalProgress })
            .inOrder()

        // throw ota exception
        otaFlow.last()
    }

    @Test(expected = IOException::class)
    fun stmOtaFailsDuringConnectSecondTime_propagatesError() = runTest {
        val expectedSteps = listOf(
            StmOtaStep.EnteringOtaModeFirstTime,
            StmOtaStep.ReconnectingAfterEnteringOtaMode,
            StmOtaStep.EnteringOtaModeSecondTime,
            StmOtaStep.CommencingTransfer,
        ) +
            OTA_PROGRESS_VALUES.map { StmOtaStep.TransferInProgress(it) } +
            listOf(StmOtaStep.ReconnectingAfterTransfer)
        val error = IOException("oops!")

        coEvery { connectionHelperMock.reconnect(any(), any()) } answers {} andThenThrows error

        val otaFlow = stmOtaHelper.performOtaSteps(scannerMock, "mac address", NEW_STM_VERSION_STRING)
        val actualSteps = otaFlow.take(expectedSteps.size).toList()

        assertThat(actualSteps).containsExactlyElementsIn(expectedSteps).inOrder()
        assertThat(actualSteps.map { it.totalProgress })
            .containsExactlyElementsIn(expectedSteps.map { it.totalProgress })
            .inOrder()

        // throws ioException
        otaFlow.last()
    }

    @Test(expected = OtaFailedException::class)
    fun stmOtaFailsToValidate_throwsOtaError() = runTest {
        val expectedSteps = listOf(
            StmOtaStep.EnteringOtaModeFirstTime,
            StmOtaStep.ReconnectingAfterEnteringOtaMode,
            StmOtaStep.EnteringOtaModeSecondTime,
            StmOtaStep.CommencingTransfer,
        ) +
            OTA_PROGRESS_VALUES.map { StmOtaStep.TransferInProgress(it) } +
            listOf(StmOtaStep.ReconnectingAfterTransfer, StmOtaStep.EnteringMainMode, StmOtaStep.ValidatingNewFirmwareVersion)

        coEvery { scannerMock.getStmFirmwareVersion() } returns OLD_STM_VERSION

        val otaFlow = stmOtaHelper.performOtaSteps(scannerMock, "mac address", NEW_STM_VERSION_STRING)
        val actualSteps = otaFlow.take(expectedSteps.size).toList()

        assertThat(actualSteps).containsExactlyElementsIn(expectedSteps).inOrder()
        assertThat(actualSteps.map { it.totalProgress })
            .containsExactlyElementsIn(expectedSteps.map { it.totalProgress })
            .inOrder()

        // throws ota exception
        otaFlow.last()
    }

    companion object {
        private const val HARDWARE_VERSION = "E-1"
        private val OTA_PROGRESS_VALUES = listOf(0.0f, 0.2f, 0.4f, 0.6f, 0.8f, 1.0f)
        private val OLD_STM_VERSION = StmExtendedFirmwareVersion("14.E-1.15")
        private const val NEW_STM_VERSION_STRING = "14.E-1.16"
        private val NEW_STM_VERSION = StmExtendedFirmwareVersion(NEW_STM_VERSION_STRING)

        private val CYPRESS_VERSION = CypressExtendedFirmwareVersion("3.E-1.4")
        private val UN20_VERSION = Un20ExtendedAppVersion("7.E-1.8")

        private val OLD_SCANNER_VERSION =
            ScannerInformation(
                hardwareVersion = HARDWARE_VERSION,
                firmwareVersions = ExtendedVersionInformation(
                    CYPRESS_VERSION,
                    OLD_STM_VERSION,
                    UN20_VERSION,
                ),
            )
        private val NEW_SCANNER_VERSION = ExtendedVersionInformation(CYPRESS_VERSION, NEW_STM_VERSION, UN20_VERSION)
    }
}
