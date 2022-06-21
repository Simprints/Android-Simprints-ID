package com.simprints.fingerprint.scanner.controllers.v2

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.scanner.adapters.v2.toScannerFirmwareVersions
import com.simprints.fingerprint.scanner.adapters.v2.toScannerVersion
import com.simprints.fingerprint.scanner.data.local.FirmwareLocalDataSource
import com.simprints.fingerprint.scanner.domain.ota.StmOtaStep
import com.simprints.fingerprint.scanner.exceptions.safe.OtaFailedException
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.models.Un20AppVersion
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.models.Un20ExtendedAppVersion
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.models.StmExtendedFirmwareVersion
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.models.StmFirmwareVersion
import com.simprints.fingerprintscanner.v2.domain.root.models.*
import com.simprints.fingerprintscanner.v2.exceptions.ota.OtaFailedException as ScannerV2OtaFailedException
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

class StmOtaHelperTest {

    private val scannerMock = mockk<Scanner>()
    private val connectionHelperMock = mockk<ConnectionHelper>()
    private val firmwareFileManagerMock = mockk<FirmwareLocalDataSource>()
    private val testScheduler = TestScheduler()
    private val stmOtaHelper = StmOtaHelper(connectionHelperMock, firmwareFileManagerMock, testScheduler)

    @Before
    fun setup() {
        every { connectionHelperMock.reconnect(any(), any()) } returns Completable.complete()

        every { scannerMock.enterStmOtaMode() } returns Completable.complete()
        every { scannerMock.startStmOta(any()) } returns Observable.fromIterable(OTA_PROGRESS_VALUES)
        every { scannerMock.getVersionInformation() } returns Single.just(OLD_SCANNER_VERSION)
        every { scannerMock.setVersionInformation(any()) } returns Completable.complete()
        every { scannerMock.enterMainMode() } returns Completable.complete()
        every { scannerMock.getStmFirmwareVersion() } returns Single.just(NEW_STM_VERSION)

        every { firmwareFileManagerMock.loadStmFirmwareBytes(NEW_STM_VERSION_STRING) } returns byteArrayOf(0x00, 0x01, 0x02, 0xFF.toByte())
    }

    @Test
    fun performStmOta_allStepsPassing_succeedsWithCorrectStepsAndProgressValues() {
        val expectedSteps = listOf(StmOtaStep.EnteringOtaModeFirstTime, StmOtaStep.ReconnectingAfterEnteringOtaMode,
            StmOtaStep.EnteringOtaModeSecondTime, StmOtaStep.CommencingTransfer) +
            OTA_PROGRESS_VALUES.map { StmOtaStep.TransferInProgress(it) } +
            listOf(StmOtaStep.ReconnectingAfterTransfer, StmOtaStep.EnteringMainMode, StmOtaStep.ValidatingNewFirmwareVersion,
                StmOtaStep.ReconnectingAfterValidating, StmOtaStep.UpdatingUnifiedVersionInformation)

        val testObserver = stmOtaHelper.performOtaSteps(scannerMock, "mac address", NEW_STM_VERSION_STRING).test()
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
    fun stmOtaFailsDuringTransfer_propagatesError() {
        val progressValues = listOf(0.0f, 0.2f, 0.4f)
        val expectedSteps = listOf(StmOtaStep.EnteringOtaModeFirstTime, StmOtaStep.ReconnectingAfterEnteringOtaMode,
            StmOtaStep.EnteringOtaModeSecondTime, StmOtaStep.CommencingTransfer) +
            progressValues.map { StmOtaStep.TransferInProgress(it) }
        val error = ScannerV2OtaFailedException("oops!")

        every { scannerMock.startStmOta(any()) } returns
            Observable.fromIterable(progressValues).concatWith(Observable.error(error))

        val testObserver = stmOtaHelper.performOtaSteps(scannerMock, "mac address", NEW_STM_VERSION_STRING).test()
        testScheduler.advanceTime()

        testObserver.awaitTerminalEvent()

        assertThat(testObserver.values()).containsExactlyElementsIn(expectedSteps).inOrder()
        assertThat(testObserver.values().map { it.totalProgress })
            .containsExactlyElementsIn(expectedSteps.map { it.totalProgress })
            .inOrder()
        testObserver.assertError(error)
    }

    @Test
    fun stmOtaFailsDuringConnectSecondTime_propagatesError() {
        val expectedSteps = listOf(StmOtaStep.EnteringOtaModeFirstTime, StmOtaStep.ReconnectingAfterEnteringOtaMode,
            StmOtaStep.EnteringOtaModeSecondTime, StmOtaStep.CommencingTransfer) +
            OTA_PROGRESS_VALUES.map { StmOtaStep.TransferInProgress(it) } +
            listOf(StmOtaStep.ReconnectingAfterTransfer)
        val error = IOException("oops!")

        every { connectionHelperMock.reconnect(any(), any()) } returnsMany
            listOf(Completable.complete(), Completable.error(error))

        val testObserver = stmOtaHelper.performOtaSteps(scannerMock, "mac address", NEW_STM_VERSION_STRING).test()
        testScheduler.advanceTime()

        testObserver.awaitTerminalEvent()

        assertThat(testObserver.values()).containsExactlyElementsIn(expectedSteps).inOrder()
        assertThat(testObserver.values().map { it.totalProgress })
            .containsExactlyElementsIn(expectedSteps.map { it.totalProgress })
            .inOrder()
        testObserver.assertError(error)
    }

    @Test
    fun stmOtaFailsToValidate_throwsOtaError() {
        val expectedSteps = listOf(StmOtaStep.EnteringOtaModeFirstTime, StmOtaStep.ReconnectingAfterEnteringOtaMode,
            StmOtaStep.EnteringOtaModeSecondTime, StmOtaStep.CommencingTransfer) +
            OTA_PROGRESS_VALUES.map { StmOtaStep.TransferInProgress(it) } +
            listOf(StmOtaStep.ReconnectingAfterTransfer, StmOtaStep.EnteringMainMode, StmOtaStep.ValidatingNewFirmwareVersion)

        every { scannerMock.getStmFirmwareVersion() } returns Single.just(OLD_STM_VERSION)

        val testObserver = stmOtaHelper.performOtaSteps(scannerMock, "mac address", NEW_STM_VERSION_STRING).test()
        testScheduler.advanceTime()

        testObserver.awaitTerminalEvent()

        assertThat(testObserver.values()).containsExactlyElementsIn(expectedSteps).inOrder()
        assertThat(testObserver.values().map { it.totalProgress })
            .containsExactlyElementsIn(expectedSteps.map { it.totalProgress })
            .inOrder()
        testObserver.assertError(OtaFailedException::class.java)
    }

    companion object {
        private const val HARDWARE_VERSION = "E-1"
        private val OTA_PROGRESS_VALUES = listOf(0.0f, 0.2f, 0.4f, 0.6f, 0.8f, 1.0f)
        private val OLD_STM_VERSION = StmExtendedFirmwareVersion("14.E-1.15")
        private const val NEW_STM_VERSION_STRING = "14.E-1.16"
        private val NEW_STM_VERSION = StmExtendedFirmwareVersion( NEW_STM_VERSION_STRING)

        private val CYPRESS_VERSION = CypressExtendedFirmwareVersion( "3.E-1.4")
        private val UN20_VERSION = Un20ExtendedAppVersion("7.E-1.8")

        private val OLD_SCANNER_VERSION =
            ScannerInformation(
                hardwareVersion = HARDWARE_VERSION,
                firmwareVersions = ScannerVersionInfo.ExtendedVersionInfo(
                    versionInfo = ExtendedVersionInformation(
                        CYPRESS_VERSION,
                        OLD_STM_VERSION,
                        UN20_VERSION
                    )
                )
            )
        private val NEW_SCANNER_VERSION = ExtendedVersionInformation(CYPRESS_VERSION, NEW_STM_VERSION, UN20_VERSION)
    }
}
