package com.simprints.fingerprint.scanner.controllers.v2

import com.simprints.fingerprint.scanner.adapters.v2.toExtendedVersionInformation
import com.simprints.fingerprint.scanner.adapters.v2.toScannerVersion
import com.simprints.fingerprint.scanner.data.local.FirmwareLocalDataSource
import com.simprints.fingerprint.scanner.domain.ota.StmOtaStep
import com.simprints.fingerprint.scanner.domain.versions.ScannerVersion
import com.simprints.fingerprint.scanner.exceptions.safe.OtaFailedException
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.models.StmExtendedFirmwareVersion
import com.simprints.fingerprintscanner.v2.scanner.Scanner
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class StmOtaHelper(private val connectionHelper: ConnectionHelper,
                   private val firmwareLocalDataSource: FirmwareLocalDataSource,
                   private val timeScheduler: Scheduler = Schedulers.io()) {

    private var newFirmwareVersion: StmExtendedFirmwareVersion? = null

    /**
     * Expects a connected scanner in root mode
     * If completes successfully, will finish with a connected scanner in root mode
     */
    fun performOtaSteps(scanner: Scanner, macAddress: String, firmwareVersion: String): Observable<StmOtaStep> =
        Observable.just<StmOtaStep>(StmOtaStep.EnteringOtaModeFirstTime)
            .concatWith(scanner.enterStmOtaMode().onErrorComplete() thenEmitStep StmOtaStep.ReconnectingAfterEnteringOtaMode)
            .concatWith(connectionHelper.reconnect(scanner, macAddress).addSmallDelay() thenEmitStep StmOtaStep.EnteringOtaModeSecondTime)
            .concatWith(scanner.enterStmOtaMode().addSmallDelay() thenEmitStep StmOtaStep.CommencingTransfer)
            .concatWith(scanner.startStmOta(firmwareLocalDataSource.loadStmFirmwareBytes(firmwareVersion)).map { StmOtaStep.TransferInProgress(it) })
            .concatWith(emitStep(StmOtaStep.ReconnectingAfterTransfer))
            .concatWith(connectionHelper.reconnect(scanner, macAddress).addSmallDelay() thenEmitStep StmOtaStep.EnteringMainMode)
            .concatWith(scanner.enterMainMode().addSmallDelay() thenEmitStep StmOtaStep.ValidatingNewFirmwareVersion)
            .concatWith(validateStmFirmwareVersion(firmwareVersion, scanner) thenEmitStep StmOtaStep.ReconnectingAfterValidating)
            .concatWith(connectionHelper.reconnect(scanner, macAddress).addSmallDelay() thenEmitStep StmOtaStep.UpdatingUnifiedVersionInformation)
            .concatWith(updateUnifiedVersionInformation(scanner))

    private fun Completable.addSmallDelay() = delay(1, TimeUnit.SECONDS, timeScheduler)

    private fun validateStmFirmwareVersion(firmwareVersion: String, scanner: Scanner): Completable =
        scanner.getStmFirmwareVersion().flatMapCompletable {
            val actualFirmwareVersion = it.versionAsString
            if (firmwareVersion != actualFirmwareVersion) {
                Completable.error(OtaFailedException("STM OTA did not increment firmware version. Expected $firmwareVersion, but was $actualFirmwareVersion"))
            } else {
                newFirmwareVersion = it
                Completable.complete()
            }
        }

    private fun updateUnifiedVersionInformation(scanner: Scanner): Completable =
        scanner.getVersionInformation().flatMapCompletable {
            newFirmwareVersion?.let { newFirmwareVersion ->
                val oldVersion = it.toScannerVersion()
                val newVersion = oldVersion.updatedWithStmVersion(newFirmwareVersion)
                scanner.setVersionInformation(newVersion.toExtendedVersionInformation())
            } ?: Completable.error(OtaFailedException("Was not able to determine the appropriate new unified version"))
        }

    private fun ScannerVersion.updatedWithStmVersion(stmFirmware: StmExtendedFirmwareVersion) =
        copy(firmware = firmware.copy(stm = stmFirmware.versionAsString))

    private fun emitStep(step: StmOtaStep) = Single.just(step)
    private infix fun Completable.thenEmitStep(step: StmOtaStep) = andThen(emitStep(step))
}
