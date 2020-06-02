package com.simprints.fingerprint.scanner.controllers.v2

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.scanner.adapters.v2.toScannerFirmwareVersions
import com.simprints.fingerprint.scanner.adapters.v2.toScannerVersion
import com.simprints.fingerprint.scanner.data.FirmwareFileManager
import com.simprints.fingerprint.scanner.domain.ota.Un20OtaStep
import com.simprints.fingerprint.scanner.exceptions.safe.OtaFailedException
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.models.Un20AppVersion
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.models.StmFirmwareVersion
import com.simprints.fingerprintscanner.v2.domain.root.models.CypressFirmwareVersion
import com.simprints.fingerprintscanner.v2.domain.root.models.UnifiedVersionInformation
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

class Un20OtaHelperTest {

    private val scannerMock = mockk<Scanner>()
    private val connectionHelperMock = mockk<ConnectionHelper>()
    private val firmwareFileManagerMock = mockk<FirmwareFileManager>()
    private val testScheduler = TestScheduler()
    private val un20OtaHelper = Un20OtaHelper(connectionHelperMock, firmwareFileManagerMock, testScheduler)

    @Before
    fun setup() {
        every { connectionHelperMock.reconnect(any(), any()) } returns Completable.complete()

        every { scannerMock.enterMainMode() } returns Completable.complete()
        every { scannerMock.turnUn20OnAndAwaitStateChangeEvent() } returns Completable.complete()
        every { scannerMock.startUn20Ota(any()) } returns Observable.fromIterable(OTA_PROGRESS_VALUES)
        every { scannerMock.turnUn20OffAndAwaitStateChangeEvent() } returns Completable.complete()
        every { scannerMock.getVersionInformation() } returns Single.just(OLD_SCANNER_VERSION)
        every { scannerMock.setVersionInformation(any()) } returns Completable.complete()
        every { scannerMock.getUn20AppVersion() } returns Single.just(NEW_UN20_VERSION)

        every { firmwareFileManagerMock.getAvailableScannerFirmwareVersions() } returns NEW_SCANNER_VERSION.toScannerFirmwareVersions()
        every { firmwareFileManagerMock.getUn20FirmwareBytes() } returns byteArrayOf(0x00, 0x01, 0x02, 0xFF.toByte())
    }

    @Test
    fun performUn20Ota_allStepsPassing_succeedsWithCorrectStepsAndProgressValues() {
        val expectedSteps = listOf(Un20OtaStep.EnteringMainMode, Un20OtaStep.TurningOnUn20BeforeTransfer, Un20OtaStep.CommencingTransfer) +
            OTA_PROGRESS_VALUES.map { Un20OtaStep.TransferInProgress(it) } +
            listOf(Un20OtaStep.AwaitingCacheCommit, Un20OtaStep.TurningOffUn20AfterTransfer, Un20OtaStep.TurningOnUn20AfterTransfer,
                Un20OtaStep.ValidatingNewFirmwareVersion, Un20OtaStep.ReconnectingAfterValidating, Un20OtaStep.UpdatingUnifiedVersionInformation)

        val testObserver = un20OtaHelper.performOtaSteps(scannerMock, "mac address").test()
        testScheduler.advanceTime()

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
    fun un20OtaFailsDuringTransfer_propagatesError() {
        val progressValues = listOf(0.0f, 0.2f, 0.4f)
        val expectedSteps = listOf(Un20OtaStep.EnteringMainMode, Un20OtaStep.TurningOnUn20BeforeTransfer, Un20OtaStep.CommencingTransfer) +
            progressValues.map { Un20OtaStep.TransferInProgress(it) }
        val error = ScannerV2OtaFailedException("oops!")

        every { scannerMock.startUn20Ota(any()) } returns
            Observable.fromIterable(progressValues).concatWith(Observable.error(error))

        val testObserver = un20OtaHelper.performOtaSteps(scannerMock, "mac address").test()
        testScheduler.advanceTime()

        testObserver.awaitTerminalEvent()

        assertThat(testObserver.values()).containsExactlyElementsIn(expectedSteps).inOrder()
        assertThat(testObserver.values().map { it.totalProgress })
            .containsExactlyElementsIn(expectedSteps.map { it.totalProgress })
            .inOrder()
        testObserver.assertError(error)
    }

    @Test
    fun un20OtaFailsDuringTurningUn20OnSecondTime_propagatesError() {
        val expectedSteps = listOf(Un20OtaStep.EnteringMainMode, Un20OtaStep.TurningOnUn20BeforeTransfer, Un20OtaStep.CommencingTransfer) +
            OTA_PROGRESS_VALUES.map { Un20OtaStep.TransferInProgress(it) } +
            listOf(Un20OtaStep.AwaitingCacheCommit, Un20OtaStep.TurningOffUn20AfterTransfer, Un20OtaStep.TurningOnUn20AfterTransfer)
        val error = IOException("oops!")

        every { scannerMock.turnUn20OnAndAwaitStateChangeEvent() } returnsMany listOf(Completable.complete(), Completable.error(error))

        val testObserver = un20OtaHelper.performOtaSteps(scannerMock, "mac address").test()
        testScheduler.advanceTime()

        testObserver.awaitTerminalEvent()

        assertThat(testObserver.values()).containsExactlyElementsIn(expectedSteps).inOrder()
        assertThat(testObserver.values().map { it.totalProgress })
            .containsExactlyElementsIn(expectedSteps.map { it.totalProgress })
            .inOrder()
        testObserver.assertError(error)
    }

    @Test
    fun un20OtaFailsToValidate_throwsOtaError() {
        val expectedSteps = listOf(Un20OtaStep.EnteringMainMode, Un20OtaStep.TurningOnUn20BeforeTransfer, Un20OtaStep.CommencingTransfer) +
            OTA_PROGRESS_VALUES.map { Un20OtaStep.TransferInProgress(it) } +
            listOf(Un20OtaStep.AwaitingCacheCommit, Un20OtaStep.TurningOffUn20AfterTransfer, Un20OtaStep.TurningOnUn20AfterTransfer,
                Un20OtaStep.ValidatingNewFirmwareVersion)

        every { scannerMock.getUn20AppVersion() } returns Single.just(OLD_UN20_VERSION)

        val testObserver = un20OtaHelper.performOtaSteps(scannerMock, "mac address").test()
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
        private val OLD_UN20_VERSION = Un20AppVersion(12, 13, 14, 15)
        private val NEW_UN20_VERSION = Un20AppVersion(18, 3, 14, 16)

        private val STM_VERSION = StmFirmwareVersion(1, 2, 3, 4)
        private val CYPRESS_VERSION = CypressFirmwareVersion(5, 6, 7, 8)

        private val OLD_SCANNER_VERSION = UnifiedVersionInformation(5066639776677915L, CYPRESS_VERSION, STM_VERSION, OLD_UN20_VERSION)
        private val NEW_SCANNER_VERSION = UnifiedVersionInformation(6755446687268892L, CYPRESS_VERSION, STM_VERSION, NEW_UN20_VERSION)
    }
}
