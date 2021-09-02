package com.simprints.fingerprint.scanner.controllers.v2

import com.simprints.fingerprint.scanner.adapters.v2.toExtendedVersionInformation
import com.simprints.fingerprint.scanner.adapters.v2.toScannerVersion
import com.simprints.fingerprint.scanner.data.local.FirmwareLocalDataSource
import com.simprints.fingerprint.scanner.domain.ota.Un20OtaStep
import com.simprints.fingerprint.scanner.domain.versions.ScannerVersion
import com.simprints.fingerprint.scanner.exceptions.safe.OtaFailedException
import com.simprints.fingerprint.tools.extensions.delayForOneSecond
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.models.Un20ExtendedAppVersion
import com.simprints.fingerprintscanner.v2.scanner.Scanner
import io.reactivex.Completable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.rx2.asFlow
import kotlinx.coroutines.rx2.await


private typealias Un20Step = FlowCollector<Un20OtaStep>


/**
 * This class handles Un20 over the air firmware update, on the fingerprint scanner.
 * It establishes connection to a scanner, in root-mode, and loads the updated firmware bytes on
 * the connected Vero 2 scanner.
 *
 * @param connectionHelper  for connection operations on the scanner
 * @param firmwareLocalDataSource  for retrieving firmware bytes to be transferred for update
 */
class Un20OtaHelper(private val connectionHelper: ConnectionHelper,
                    private val firmwareLocalDataSource: FirmwareLocalDataSource) {

    private var newFirmwareVersion: Un20ExtendedAppVersion? = null
    /**
     * This function is responsible for performing Un20 firmware updates on Vero 2 Scanner, and
     * emits a sequence of each step [Un20OtaStep] being processed.
     *
     * If completes successfully, will finish with a connected scanner in root mode
     *
     * @param scanner the connected scanner expected to be in root mode
     * @param macAddress the scanner's macAddress
     */
    fun performOtaSteps(scanner: Scanner, macAddress: String): Flow<Un20OtaStep> = flow {
        enterMainMode(scanner)
        turnOnUn20BeforeTransfer(scanner)
        transferFirmwareBytes(scanner)
        awaitCacheCommit()
        turnOffUn20AfterTransfer(scanner)
        turnOnUn20AfterTransfer(scanner)
        validateRunningFirmwareVersion(scanner)
        reconnectScannerAfterValidating(scanner, macAddress)
        updateFirmwareVersion(scanner)
    }
/*
 fun performOtaSteps(scanner: Scanner, macAddress: String, firmwareVersion: String): Observable<Un20OtaStep> =
        Observable.just<Un20OtaStep>(Un20OtaStep.EnteringMainMode)
            .concatWith(scanner.enterMainMode().addSmallDelay() thenEmitStep Un20OtaStep.TurningOnUn20BeforeTransfer)
            .concatWith(scanner.turnUn20OnAndAwaitStateChangeEvent() thenEmitStep Un20OtaStep.CommencingTransfer)
            .concatWith(scanner.startUn20Ota(firmwareLocalDataSource.loadUn20FirmwareBytes(firmwareVersion)).map { Un20OtaStep.TransferInProgress(it) })
            .concatWith(emitStep(Un20OtaStep.AwaitingCacheCommit))
            .concatWith(waitCacheCommitTime() thenEmitStep Un20OtaStep.TurningOffUn20AfterTransfer)
            .concatWith(scanner.turnUn20OffAndAwaitStateChangeEvent().addSmallDelay() thenEmitStep Un20OtaStep.TurningOnUn20AfterTransfer)
            .concatWith(scanner.turnUn20OnAndAwaitStateChangeEvent() thenEmitStep Un20OtaStep.ValidatingNewFirmwareVersion)
            .concatWith(validateUn20FirmwareVersion(firmwareVersion, scanner) thenEmitStep Un20OtaStep.ReconnectingAfterValidating)
            .concatWith(rxCompletable { connectionHelper.reconnect(scanner, macAddress) }.addSmallDelay() thenEmitStep Un20OtaStep.UpdatingUnifiedVersionInformation)
            .concatWith(updateUnifiedVersionInformation(scanner))

 */
    private suspend fun Un20Step.enterMainMode(scanner: Scanner) {
        emit(Un20OtaStep.EnteringMainMode)
        scanner.enterMainMode().await()
        delayForOneSecond()
    }

    private suspend fun Un20Step.turnOnUn20BeforeTransfer(scanner: Scanner) {
        emit(Un20OtaStep.TurningOnUn20BeforeTransfer)
        scanner.turnUn20OnAndAwaitStateChangeEvent().await()
    }

    private suspend fun Un20Step.transferFirmwareBytes(scanner: Scanner) {
        emit(Un20OtaStep.CommencingTransfer)
        scanner.startUn20Ota(firmwareLocalDataSource.loadUn20FirmwareBytes())
            .map { Un20OtaStep.TransferInProgress(it) }
            .asFlow()
            .collect { emit(it) }
    }

    private suspend fun Un20Step.awaitCacheCommit() {
        emit(Un20OtaStep.AwaitingCacheCommit)
        waitCacheCommitTime()
    }

    private suspend fun Un20Step.turnOffUn20AfterTransfer(scanner: Scanner) {
        emit(Un20OtaStep.TurningOffUn20AfterTransfer)
        scanner.turnUn20OffAndAwaitStateChangeEvent().await()
        delayForOneSecond()
    }

    private suspend fun Un20Step.turnOnUn20AfterTransfer(scanner: Scanner) {
        emit(Un20OtaStep.TurningOnUn20AfterTransfer)
        scanner.turnUn20OnAndAwaitStateChangeEvent().await()
    }

    private suspend fun Un20Step.validateRunningFirmwareVersion(scanner: Scanner) {
        emit(Un20OtaStep.ValidatingNewFirmwareVersion)
        validateUn20FirmwareVersion(firmwareVersion: String, scanner)
    }

    private suspend fun Un20Step.reconnectScannerAfterValidating(scanner: Scanner, macAddress: String) {
        emit(Un20OtaStep.ReconnectingAfterValidating)
        connectionHelper.reconnect(scanner, macAddress)
        delayForOneSecond()
    }

    private suspend fun Un20Step.updateFirmwareVersion(scanner: Scanner) {
        emit(Un20OtaStep.UpdatingUnifiedVersionInformation)
        updateUnifiedVersionInformation(scanner)
    }

    private suspend fun validateUn20FirmwareVersion(scanner: Scanner) =
        scanner.getUn20AppVersion().flatMapCompletable {
            val actualFirmwareVersion = it.versionAsString
            if (firmwareVersion != actualFirmwareVersion) {
                Completable.error(OtaFailedException("UN20 OTA did not increment firmware version. Expected $firmwareVersion, but was $actualFirmwareVersion"))
            } else {
                newFirmwareVersion = it
                Completable.complete()
            }
        }.await()

    private suspend fun updateUnifiedVersionInformation(scanner: Scanner) =
        scanner.getVersionInformation().flatMapCompletable {
            newFirmwareVersion?.let { newFirmwareVersion ->
                val oldVersion = it.toScannerVersion()
                val newVersion = oldVersion.updatedWithUn20Version(newFirmwareVersion)
                scanner.setVersionInformation(newVersion.toExtendedVersionInformation())
            } ?: Completable.error(OtaFailedException("Was not able to determine the appropriate new unified version"))
        }.await()

    private fun ScannerVersion.updatedWithUn20Version(un20Firmware: Un20ExtendedAppVersion) =
        copy(firmware = firmware.copy(un20 = un20Firmware.versionAsString))

    private suspend fun waitCacheCommitTime() = delay(CACHE_COMMIT_TIME)

    companion object {
        const val CACHE_COMMIT_TIME = 10000L // milliseconds
    }
}
