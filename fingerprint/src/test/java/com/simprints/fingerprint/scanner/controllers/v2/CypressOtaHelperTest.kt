package com.simprints.fingerprint.scanner.controllers.v2

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.scanner.adapters.v2.toScannerFirmwareVersions
import com.simprints.fingerprint.scanner.data.local.FirmwareLocalDataSource
import com.simprints.fingerprint.scanner.domain.ota.CypressOtaStep
import com.simprints.fingerprint.scanner.domain.ota.DownloadableFirmwareVersion
import com.simprints.fingerprint.scanner.exceptions.safe.OtaFailedException
import com.simprints.fingerprintscanner.v2.domain.root.models.*
import com.simprints.fingerprintscanner.v2.scanner.Scanner
import io.mockk.*
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import java.io.IOException
import com.simprints.fingerprintscanner.v2.exceptions.ota.OtaFailedException as ScannerV2OtaFailedException

class CypressOtaHelperTest {

    private val scannerMock = mockk<Scanner>()
    private val connectionHelperMock = mockk<ConnectionHelper>()
    private val firmwareFileManagerMock = mockk<FirmwareLocalDataSource>()
    private val cypressOtaHelper = CypressOtaHelper(connectionHelperMock, firmwareFileManagerMock)

    @Before
    fun setup() {
        coEvery { connectionHelperMock.reconnect(any(), any()) } answers {}


        every { scannerMock.enterCypressOtaMode() } returns Completable.complete()
        every { scannerMock.startCypressOta(any()) } returns Observable.fromIterable(OTA_PROGRESS_VALUES)
        every { scannerMock.getVersionInformation() } returns Single.just(OLD_SCANNER_VERSION)
        every { scannerMock.setVersionInformation(any()) } returns Completable.complete()
        every { scannerMock.getCypressFirmwareVersion() } returns Single.just(OLD_CYPRESS_VERSION)
        every { scannerMock.getCypressExtendedFirmwareVersion() } returns Single.just(NEW_CYPRESS_VERSION)

        every {
            firmwareFileManagerMock.getAvailableScannerFirmwareVersions()
        } returns mutableMapOf(
            DownloadableFirmwareVersion.Chip.CYPRESS to mutableSetOf(NEW_CYPRESS_VERSION_STRING)
        )
        every { firmwareFileManagerMock.loadCypressFirmwareBytes(NEW_CYPRESS_VERSION_STRING) } returns byteArrayOf(0x00, 0x01, 0x02, 0xFF.toByte())
    }

    @Test
    fun performCypressOta_allStepsPassing_succeedsWithCorrectStepsAndProgressValues() = runBlocking {
        val expectedSteps = listOf(CypressOtaStep.EnteringOtaMode, CypressOtaStep.CommencingTransfer) +
            OTA_PROGRESS_VALUES.map { CypressOtaStep.TransferInProgress(it) } +
            listOf(CypressOtaStep.ReconnectingAfterTransfer, CypressOtaStep.ValidatingNewFirmwareVersion, CypressOtaStep.UpdatingUnifiedVersionInformation)

        val actualItems = cypressOtaHelper.performOtaSteps(scannerMock, "mac address", NEW_CYPRESS_VERSION_STRING).toList()


        assertThat(actualItems).containsExactlyElementsIn(expectedSteps).inOrder()
        assertThat(actualItems.map { it.totalProgress })
            .containsExactlyElementsIn(expectedSteps.map { it.totalProgress })
            .inOrder()

        val sentUnifiedVersion = CapturingSlot<ExtendedVersionInformation>()
        verify { scannerMock.setVersionInformation(capture(sentUnifiedVersion)) }
        assertThat(sentUnifiedVersion.captured.toScannerFirmwareVersions())
            .isEqualTo(NEW_SCANNER_VERSION.firmwareVersions.toScannerFirmwareVersions())
    }

    @Test(expected = ScannerV2OtaFailedException::class)
    fun
        cypressOtaFailsDuringTransfer_propagatesError() = runBlocking<Unit> {
        val progressValues = listOf(0.0f, 0.2f, 0.4f)
        val expectedSteps = listOf(CypressOtaStep.EnteringOtaMode, CypressOtaStep.CommencingTransfer) +
            progressValues.map { CypressOtaStep.TransferInProgress(it) }
        val error = ScannerV2OtaFailedException("oops!")

        every { scannerMock.startCypressOta(any()) } returns
            Observable.fromIterable(progressValues).concatWith(Observable.error(error))

        val otaFlow = cypressOtaHelper.performOtaSteps(scannerMock, "mac address", NEW_CYPRESS_VERSION_STRING)
        val actualItems = otaFlow
            .take(expectedSteps.size)
            .toList()


        assertThat(actualItems).containsExactlyElementsIn(expectedSteps).inOrder()
        assertThat(actualItems.map { it.totalProgress })
            .containsExactlyElementsIn(expectedSteps.map { it.totalProgress })
            .inOrder()

        // should throw exception
        otaFlow.last()
    }

    @Test(expected = IOException::class)
    fun cypressOtaFailsDuringConnect_propagatesError() = runBlocking<Unit> {
        val expectedSteps = listOf(CypressOtaStep.EnteringOtaMode, CypressOtaStep.CommencingTransfer) +
            OTA_PROGRESS_VALUES.map { CypressOtaStep.TransferInProgress(it) } +
            listOf(CypressOtaStep.ReconnectingAfterTransfer)
        val error = IOException("oops!")

        coEvery { connectionHelperMock.reconnect(any(), any()) } throws  error
        val otaFlow =  cypressOtaHelper.performOtaSteps(scannerMock, "mac address", NEW_CYPRESS_VERSION_STRING)
        val actualSteps = otaFlow.take(expectedSteps.size).toList()

        assertThat(actualSteps).containsExactlyElementsIn(expectedSteps).inOrder()
        assertThat(actualSteps.map { it.totalProgress })
            .containsExactlyElementsIn(expectedSteps.map { it.totalProgress })
            .inOrder()

        // throw ioException
        otaFlow.last()
    }

    @Test(expected = OtaFailedException::class)
    fun cypressOtaFailsToValidate_throwsOtaError() = runBlocking<Unit> {
        val expectedSteps = listOf(CypressOtaStep.EnteringOtaMode, CypressOtaStep.CommencingTransfer) +
            OTA_PROGRESS_VALUES.map { CypressOtaStep.TransferInProgress(it) } +
            listOf(CypressOtaStep.ReconnectingAfterTransfer, CypressOtaStep.ValidatingNewFirmwareVersion)

        every { scannerMock.getCypressExtendedFirmwareVersion() } returns Single.just(
            CypressExtendedFirmwareVersion(versionAsString = "")
        )

        val otaFlow = cypressOtaHelper.performOtaSteps(scannerMock, "mac address", NEW_CYPRESS_VERSION_STRING)
        val actualSteps = otaFlow.take(expectedSteps.size).toList()

        assertThat(actualSteps).containsExactlyElementsIn(expectedSteps).inOrder()
        assertThat(actualSteps.map { it.totalProgress })
            .containsExactlyElementsIn(expectedSteps.map { it.totalProgress })
            .inOrder()

        // throws Ota exception
        otaFlow.last()
    }

    companion object {
        private val OTA_PROGRESS_VALUES = listOf(0.0f, 0.2f, 0.4f, 0.6f, 0.8f, 1.0f)
        private val OLD_CYPRESS_VERSION = CypressFirmwareVersion(12, 13, 14, 15)

        private const val NEW_CYPRESS_VERSION_STRING =  "14.E-1.16"
        private val NEW_CYPRESS_VERSION = CypressExtendedFirmwareVersion(versionAsString = NEW_CYPRESS_VERSION_STRING)

        private const val HARDWARE_VERSION = "E-1"

        private val OLD_SCANNER_VERSION = ScannerInformation(
            hardwareVersion = HARDWARE_VERSION,
            firmwareVersions = UnifiedVersionInformation(
                5066639776677915L,
                OLD_CYPRESS_VERSION,
                mockk(relaxed = true),
                mockk(relaxed = true)
            ).toExtendedVersionInfo()
        )


        private val NEW_SCANNER_VERSION = ScannerInformation(
            hardwareVersion = HARDWARE_VERSION,
            firmwareVersions = ExtendedVersionInformation(
                cypressFirmwareVersion = NEW_CYPRESS_VERSION,
                stmFirmwareVersion = mockk(relaxed = true),
                un20AppVersion = mockk(relaxed = true)
            )
        )
    }
}
