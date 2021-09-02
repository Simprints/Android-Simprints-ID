package com.simprints.fingerprint.scanner.controllers.v2

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.scanner.adapters.v2.toScannerFirmwareVersions
import com.simprints.fingerprint.scanner.data.local.FirmwareLocalDataSource
import com.simprints.fingerprint.scanner.domain.ota.Un20OtaStep
import com.simprints.fingerprint.scanner.exceptions.safe.OtaFailedException
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.models.Un20ExtendedAppVersion
import com.simprints.fingerprintscanner.v2.domain.root.models.ExtendedVersionInformation
import com.simprints.fingerprintscanner.v2.domain.root.models.ScannerInformation
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

class Un20OtaHelperTest {

    private val scannerMock = mockk<Scanner>()
    private val connectionHelperMock = mockk<ConnectionHelper>()
    private val firmwareFileManagerMock = mockk<FirmwareLocalDataSource>()
    private val un20OtaHelper = Un20OtaHelper(connectionHelperMock, firmwareFileManagerMock)

    @Before
    fun setup() {
        coEvery { connectionHelperMock.reconnect(any(), any()) } answers {}

        every { scannerMock.enterMainMode() } returns Completable.complete()
        every { scannerMock.turnUn20OnAndAwaitStateChangeEvent() } returns Completable.complete()
        every { scannerMock.startUn20Ota(any()) } returns Observable.fromIterable(OTA_PROGRESS_VALUES)
        every { scannerMock.turnUn20OffAndAwaitStateChangeEvent() } returns Completable.complete()
        every { scannerMock.getVersionInformation() } returns Single.just(OLD_SCANNER_INFORMATION)
        every { scannerMock.setVersionInformation(any()) } returns Completable.complete()
        every { scannerMock.getUn20AppVersion() } returns Single.just(NEW_UN20_VERSION)

        every { firmwareFileManagerMock.loadUn20FirmwareBytes(NEW_UN20_VERSION_STRING) } returns byteArrayOf(0x00, 0x01, 0x02, 0xFF.toByte())
    }

    @Test
    fun performUn20Ota_allStepsPassing_succeedsWithCorrectStepsAndProgressValues() = runBlocking {
        val expectedSteps = listOf(Un20OtaStep.EnteringMainMode, Un20OtaStep.TurningOnUn20BeforeTransfer, Un20OtaStep.CommencingTransfer) +
            OTA_PROGRESS_VALUES.map { Un20OtaStep.TransferInProgress(it) } +
            listOf(Un20OtaStep.AwaitingCacheCommit, Un20OtaStep.TurningOffUn20AfterTransfer, Un20OtaStep.TurningOnUn20AfterTransfer,
                Un20OtaStep.ValidatingNewFirmwareVersion, Un20OtaStep.ReconnectingAfterValidating, Un20OtaStep.UpdatingUnifiedVersionInformation)

        val actualSteps = un20OtaHelper.performOtaSteps(scannerMock, "mac address", NEW_UN20_VERSION_STRING).toList()

        assertThat(actualSteps).containsExactlyElementsIn(expectedSteps).inOrder()
        assertThat(actualSteps.map { it.totalProgress })
            .containsExactlyElementsIn(expectedSteps.map { it.totalProgress })
            .inOrder()

        val sentUnifiedVersion = CapturingSlot<ExtendedVersionInformation>()
        verify { scannerMock.setVersionInformation(capture(sentUnifiedVersion)) }
        assertThat(sentUnifiedVersion.captured.toScannerFirmwareVersions()).isEqualTo(NEW_SCANNER_VERSION.toScannerFirmwareVersions())
    }

    @Test(expected = ScannerV2OtaFailedException::class)
    fun un20OtaFailsDuringTransfer_propagatesError() = runBlocking<Unit> {
        val progressValues = listOf(0.0f, 0.2f, 0.4f)
        val expectedSteps = listOf(Un20OtaStep.EnteringMainMode, Un20OtaStep.TurningOnUn20BeforeTransfer, Un20OtaStep.CommencingTransfer) +
            progressValues.map { Un20OtaStep.TransferInProgress(it) }
        val error = ScannerV2OtaFailedException("oops!")

        every { scannerMock.startUn20Ota(any()) } returns
            Observable.fromIterable(progressValues).concatWith(Observable.error(error))

        val otaFlow =  un20OtaHelper.performOtaSteps(scannerMock, "mac address", NEW_UN20_VERSION_STRING)
        val actualSteps = otaFlow.take(expectedSteps.size).toList()

        assertThat(actualSteps).containsExactlyElementsIn(expectedSteps).inOrder()
        assertThat(actualSteps.map { it.totalProgress })
            .containsExactlyElementsIn(expectedSteps.map { it.totalProgress })
            .inOrder()

        // should throw scanner exception
        otaFlow.last()
    }

    @Test(expected = IOException::class)
    fun un20OtaFailsDuringTurningUn20OnSecondTime_propagatesError() = runBlocking<Unit> {
        val expectedSteps = listOf(Un20OtaStep.EnteringMainMode, Un20OtaStep.TurningOnUn20BeforeTransfer, Un20OtaStep.CommencingTransfer) +
            OTA_PROGRESS_VALUES.map { Un20OtaStep.TransferInProgress(it) } +
            listOf(Un20OtaStep.AwaitingCacheCommit, Un20OtaStep.TurningOffUn20AfterTransfer, Un20OtaStep.TurningOnUn20AfterTransfer)
        val error = IOException("oops!")

        every { scannerMock.turnUn20OnAndAwaitStateChangeEvent() } returnsMany listOf(Completable.complete(), Completable.error(error))

        val otaFlow = un20OtaHelper.performOtaSteps(scannerMock, "mac address", NEW_UN20_VERSION_STRING)
        val actualSteps = otaFlow.take(expectedSteps.size).toList()


        assertThat(actualSteps).containsExactlyElementsIn(expectedSteps).inOrder()
        assertThat(actualSteps.map { it.totalProgress })
            .containsExactlyElementsIn(expectedSteps.map { it.totalProgress })
            .inOrder()


        // throws ioException
        otaFlow.last()
    }

    @Test(expected = OtaFailedException::class)
    fun un20OtaFailsToValidate_throwsOtaError() = runBlocking<Unit> {
        val expectedSteps = listOf(Un20OtaStep.EnteringMainMode, Un20OtaStep.TurningOnUn20BeforeTransfer, Un20OtaStep.CommencingTransfer) +
            OTA_PROGRESS_VALUES.map { Un20OtaStep.TransferInProgress(it) } +
            listOf(Un20OtaStep.AwaitingCacheCommit, Un20OtaStep.TurningOffUn20AfterTransfer, Un20OtaStep.TurningOnUn20AfterTransfer,
                Un20OtaStep.ValidatingNewFirmwareVersion)

        every { scannerMock.getUn20AppVersion() } returns Single.just(OLD_UN20_VERSION)

        val otaFlow = un20OtaHelper.performOtaSteps(scannerMock, "mac address", NEW_UN20_VERSION_STRING)
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
        private val OLD_UN20_VERSION = Un20ExtendedAppVersion( "14.E-1.15")
        private const val NEW_UN20_VERSION_STRING = "14.E-1.16"
        private val NEW_UN20_VERSION = Un20ExtendedAppVersion( NEW_UN20_VERSION_STRING)

        private val OLD_SCANNER_INFORMATION =
            ScannerInformation(
                hardwareVersion = HARDWARE_VERSION,
                firmwareVersions = ExtendedVersionInformation(
                    mockk(relaxed = true),
                    mockk(relaxed = true),
                    OLD_UN20_VERSION
                )
            )
        private val NEW_SCANNER_VERSION = ExtendedVersionInformation(
            mockk(relaxed = true),
            mockk(relaxed = true),
            NEW_UN20_VERSION
        )
    }
}
