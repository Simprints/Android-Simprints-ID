package com.simprints.fingerprint.scanner.controllers.v2

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.scanner.adapters.v2.toScannerFirmwareVersions
import com.simprints.fingerprint.scanner.data.local.FirmwareLocalDataSource
import com.simprints.fingerprint.scanner.domain.ota.CypressOtaStep
import com.simprints.fingerprint.scanner.domain.ota.DownloadableFirmwareVersion
import com.simprints.fingerprint.scanner.exceptions.safe.OtaFailedException
import com.simprints.fingerprintscanner.v2.domain.root.models.*
import com.simprints.fingerprintscanner.v2.scanner.Scanner
import com.simprints.testtools.common.reactive.advanceTime
import com.simprints.testtools.common.syntax.awaitAndAssertSuccess
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.TestScheduler
import org.junit.Before
import org.junit.Test
import java.io.IOException
import com.simprints.fingerprintscanner.v2.exceptions.ota.OtaFailedException as ScannerV2OtaFailedException

class CypressOtaHelperTest {

    private val scannerMock = mockk<Scanner>()
    private val connectionHelperMock = mockk<ConnectionHelper>()
    private val firmwareFileManagerMock = mockk<FirmwareLocalDataSource>()
    private val testScheduler = TestScheduler()
    private val cypressOtaHelper = CypressOtaHelper(connectionHelperMock, firmwareFileManagerMock, testScheduler)

    @Before
    fun setup() {
        every { connectionHelperMock.reconnect(any(), any()) } returns Completable.complete()

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
    fun performCypressOta_allStepsPassing_succeedsWithCorrectStepsAndProgressValues() {
        val expectedSteps = listOf(CypressOtaStep.EnteringOtaMode, CypressOtaStep.CommencingTransfer) +
            OTA_PROGRESS_VALUES.map { CypressOtaStep.TransferInProgress(it) } +
            listOf(CypressOtaStep.ReconnectingAfterTransfer, CypressOtaStep.ValidatingNewFirmwareVersion, CypressOtaStep.UpdatingUnifiedVersionInformation)

        val testObserver = cypressOtaHelper.performOtaSteps(scannerMock, "mac address", NEW_CYPRESS_VERSION_STRING).test()
        testScheduler.advanceTime()

        testObserver.awaitAndAssertSuccess()

        assertThat(testObserver.values()).containsExactlyElementsIn(expectedSteps).inOrder()
        assertThat(testObserver.values().map { it.totalProgress })
            .containsExactlyElementsIn(expectedSteps.map { it.totalProgress })
            .inOrder()

        val sentUnifiedVersion = CapturingSlot<ExtendedVersionInformation>()
        verify { scannerMock.setVersionInformation(capture(sentUnifiedVersion)) }
        assertThat(sentUnifiedVersion.captured.toScannerFirmwareVersions()).isEqualTo(NEW_SCANNER_VERSION.toScannerFirmwareVersions())
    }

    @Test
    fun
        cypressOtaFailsDuringTransfer_propagatesError() {
        val progressValues = listOf(0.0f, 0.2f, 0.4f)
        val expectedSteps = listOf(CypressOtaStep.EnteringOtaMode, CypressOtaStep.CommencingTransfer) +
            progressValues.map { CypressOtaStep.TransferInProgress(it) }
        val error = ScannerV2OtaFailedException("oops!")

        every { scannerMock.startCypressOta(any()) } returns
            Observable.fromIterable(progressValues).concatWith(Observable.error(error))

        val testObserver = cypressOtaHelper.performOtaSteps(scannerMock, "mac address", NEW_CYPRESS_VERSION_STRING).test()
        testScheduler.advanceTime()

        testObserver.awaitTerminalEvent()

        assertThat(testObserver.values()).containsExactlyElementsIn(expectedSteps).inOrder()
        assertThat(testObserver.values().map { it.totalProgress })
            .containsExactlyElementsIn(expectedSteps.map { it.totalProgress })
            .inOrder()
        testObserver.assertError(error)
    }

    @Test
    fun cypressOtaFailsDuringConnect_propagatesError() {
        val expectedSteps = listOf(CypressOtaStep.EnteringOtaMode, CypressOtaStep.CommencingTransfer) +
            OTA_PROGRESS_VALUES.map { CypressOtaStep.TransferInProgress(it) } +
            listOf(CypressOtaStep.ReconnectingAfterTransfer)
        val error = IOException("oops!")

        every { connectionHelperMock.reconnect(any(), any()) } returns Completable.error(error)

        val testObserver = cypressOtaHelper.performOtaSteps(scannerMock, "mac address", NEW_CYPRESS_VERSION_STRING).test()
        testScheduler.advanceTime()

        testObserver.awaitTerminalEvent()

        assertThat(testObserver.values()).containsExactlyElementsIn(expectedSteps).inOrder()
        assertThat(testObserver.values().map { it.totalProgress })
            .containsExactlyElementsIn(expectedSteps.map { it.totalProgress })
            .inOrder()
        testObserver.assertError(error)
    }

    @Test
    fun cypressOtaFailsToValidate_throwsOtaError() {
        val expectedSteps = listOf(CypressOtaStep.EnteringOtaMode, CypressOtaStep.CommencingTransfer) +
            OTA_PROGRESS_VALUES.map { CypressOtaStep.TransferInProgress(it) } +
            listOf(CypressOtaStep.ReconnectingAfterTransfer, CypressOtaStep.ValidatingNewFirmwareVersion)

        every { scannerMock.getCypressExtendedFirmwareVersion() } returns Single.just(
            CypressExtendedFirmwareVersion(versionAsString = "")
        )

        val testObserver = cypressOtaHelper.performOtaSteps(scannerMock, "mac address", NEW_CYPRESS_VERSION_STRING).test()
        testScheduler.advanceTime()

        testObserver.awaitTerminalEvent()

        assertThat(testObserver.values()).containsExactlyElementsIn(expectedSteps).inOrder()
        assertThat(testObserver.values().map { it.totalProgress })
            .containsExactlyElementsIn(expectedSteps.map { it.totalProgress })
            .inOrder()
        testObserver.assertError(OtaFailedException::class.java)
    }

    companion object {
        private val OTA_PROGRESS_VALUES = listOf(0.0f, 0.2f, 0.4f, 0.6f, 0.8f, 1.0f)
        private val OLD_CYPRESS_VERSION = CypressFirmwareVersion(12, 13, 14, 15)

        private const val NEW_CYPRESS_VERSION_STRING =  "14.E-1.16"
        private val NEW_CYPRESS_VERSION = CypressExtendedFirmwareVersion(versionAsString = NEW_CYPRESS_VERSION_STRING)

        private const val HARDWARE_VERSION = "E-1"

        private val OLD_SCANNER_VERSION = ScannerInformation(
            hardwareVersion = HARDWARE_VERSION,
            firmwareVersions = ScannerVersionInfo.LegacyVersionInfo(
               versionInfo = UnifiedVersionInformation(5066639776677915L,
                   OLD_CYPRESS_VERSION, mockk(relaxed = true), mockk(relaxed = true))
            )
        )

        private val NEW_SCANNER_VERSION = ScannerInformation(
            hardwareVersion = HARDWARE_VERSION,
            firmwareVersions = ScannerVersionInfo.ExtendedVersionInfo(
                ExtendedVersionInformation(
                    cypressFirmwareVersion = NEW_CYPRESS_VERSION,
                    stmFirmwareVersion = mockk(relaxed = true),
                    un20AppVersion = mockk(relaxed = true)
                )
            )
        )
    }
}
