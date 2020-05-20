package com.simprints.fingerprint.scanner.controllers.v2

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.scanner.adapters.v2.toScannerFirmwareVersions
import com.simprints.fingerprint.scanner.adapters.v2.toScannerVersion
import com.simprints.fingerprint.scanner.data.FirmwareFileManager
import com.simprints.fingerprint.scanner.domain.ota.CypressOtaStep
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.models.Un20AppVersion
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.models.StmFirmwareVersion
import com.simprints.fingerprintscanner.v2.domain.root.models.CypressFirmwareVersion
import com.simprints.fingerprintscanner.v2.domain.root.models.UnifiedVersionInformation
import com.simprints.fingerprintscanner.v2.exceptions.ota.OtaFailedException
import com.simprints.fingerprintscanner.v2.scanner.Scanner
import com.simprints.testtools.common.syntax.awaitAndAssertSuccess
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.Before
import org.junit.Test
import java.io.IOException

class CypressOtaHelperTest {

    private val scannerMock = mockk<Scanner>()
    private val connectionHelperMock = mockk<ConnectionHelper>()
    private val firmwareFileManagerMock = mockk<FirmwareFileManager>()
    private val cypressOtaHelper = CypressOtaHelper(connectionHelperMock, firmwareFileManagerMock)

    @Before
    fun setup() {
        every { connectionHelperMock.reconnect(any(), any()) } returns Completable.complete()

        every { scannerMock.enterCypressOtaMode() } returns Completable.complete()
        every { scannerMock.startCypressOta(any()) } returns Observable.fromIterable(OTA_PROGRESS_VALUES)
        every { scannerMock.getVersionInformation() } returns Single.just(OLD_SCANNER_VERSION)
        every { scannerMock.setVersionInformation(any()) } returns Completable.complete()
        every { scannerMock.getCypressFirmwareVersion() } returns Single.just(NEW_CYPRESS_VERSION)

        every { firmwareFileManagerMock.getAvailableScannerFirmwareVersions() } returns NEW_SCANNER_VERSION.toScannerFirmwareVersions()
        every { firmwareFileManagerMock.getCypressFirmwareBytes() } returns byteArrayOf(0x00, 0x01, 0x02, 0xFF.toByte())
    }

    @Test
    fun performCypressOta_allStepsPassing_succeedsWithCorrectStepsAndProgressValues() {
        val expectedSteps = listOf(CypressOtaStep.Started, CypressOtaStep.OtaModeEntered) +
            OTA_PROGRESS_VALUES.map { CypressOtaStep.TransferInProgress(it) } +
            listOf(CypressOtaStep.ReconnectingAfterTransfer, CypressOtaStep.ValidatingNewFirmwareVersion,
                CypressOtaStep.ReconnectingAfterValidating, CypressOtaStep.UpdatingUnifiedVersionInformation)

        val testObserver = cypressOtaHelper.performOtaSteps(scannerMock, "mac address").test()

        testObserver.awaitAndAssertSuccess()

        assertThat(testObserver.values()).containsExactlyElementsIn(expectedSteps).inOrder()
        assertThat(testObserver.values().map { it.totalProgress })
            .containsExactlyElementsIn(expectedSteps.map { it.totalProgress })
            .inOrder()

        val sentUnifiedVersion = CapturingSlot<UnifiedVersionInformation>()
        verify { scannerMock.setVersionInformation(capture(sentUnifiedVersion)) }
        assertThat(sentUnifiedVersion.captured.toScannerVersion()).isEqualTo(NEW_SCANNER_VERSION.toScannerVersion())
        assertThat(sentUnifiedVersion.captured.masterFirmwareVersion).isEqualTo(NEW_SCANNER_VERSION.masterFirmwareVersion)
    }

    @Test
    fun cypressOtaFailsDuringTransfer_propagatesError() {
        val progressValues = listOf(0.0f, 0.2f, 0.4f)
        val expectedSteps = listOf(CypressOtaStep.Started, CypressOtaStep.OtaModeEntered) +
            progressValues.map { CypressOtaStep.TransferInProgress(it) }
        val error = OtaFailedException("oops!")

        every { scannerMock.startCypressOta(any()) } returns
            Observable.fromIterable(progressValues).concatWith(Observable.error(error))

        val testObserver = cypressOtaHelper.performOtaSteps(scannerMock, "mac address").test()

        testObserver.awaitTerminalEvent()

        assertThat(testObserver.values()).containsExactlyElementsIn(expectedSteps).inOrder()
        assertThat(testObserver.values().map { it.totalProgress })
            .containsExactlyElementsIn(expectedSteps.map { it.totalProgress })
            .inOrder()
        testObserver.assertError(error)
    }

    @Test
    fun cypressOtaFailsDuringConnect_propagatesError() {
        val expectedSteps = listOf(CypressOtaStep.Started, CypressOtaStep.OtaModeEntered) +
            OTA_PROGRESS_VALUES.map { CypressOtaStep.TransferInProgress(it) } +
            listOf(CypressOtaStep.ReconnectingAfterTransfer)
        val error = IOException("oops!")

        every { connectionHelperMock.reconnect(any(), any()) } returns Completable.error(error)

        val testObserver = cypressOtaHelper.performOtaSteps(scannerMock, "mac address").test()

        testObserver.awaitTerminalEvent()

        assertThat(testObserver.values()).containsExactlyElementsIn(expectedSteps).inOrder()
        assertThat(testObserver.values().map { it.totalProgress })
            .containsExactlyElementsIn(expectedSteps.map { it.totalProgress })
            .inOrder()
        testObserver.assertError(error)
    }

    @Test
    fun cypressOtaFailsToValidate_throwsOtaError() {
        val expectedSteps = listOf(CypressOtaStep.Started, CypressOtaStep.OtaModeEntered) +
            OTA_PROGRESS_VALUES.map { CypressOtaStep.TransferInProgress(it) } +
            listOf(CypressOtaStep.ReconnectingAfterTransfer, CypressOtaStep.ValidatingNewFirmwareVersion)

        every { scannerMock.getCypressFirmwareVersion() } returns Single.just(OLD_CYPRESS_VERSION)

        val testObserver = cypressOtaHelper.performOtaSteps(scannerMock, "mac address").test()

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
        private val NEW_CYPRESS_VERSION = CypressFirmwareVersion(18, 3, 14, 16)

        private val STM_VERSION = StmFirmwareVersion(1, 2, 3, 4)
        private val UN20_VERSION = Un20AppVersion(5, 6, 7, 8)

        private val OLD_SCANNER_VERSION = UnifiedVersionInformation(5066639776677915L, OLD_CYPRESS_VERSION, STM_VERSION, UN20_VERSION)
        private val NEW_SCANNER_VERSION = UnifiedVersionInformation(6755446687268892L, NEW_CYPRESS_VERSION, STM_VERSION, UN20_VERSION)
    }
}
