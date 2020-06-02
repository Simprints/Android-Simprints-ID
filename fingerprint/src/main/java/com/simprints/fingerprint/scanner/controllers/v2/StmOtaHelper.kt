package com.simprints.fingerprint.scanner.controllers.v2

import com.simprints.fingerprint.scanner.adapters.v2.toChipApiVersion
import com.simprints.fingerprint.scanner.adapters.v2.toChipFirmwareVersion
import com.simprints.fingerprint.scanner.adapters.v2.toScannerVersion
import com.simprints.fingerprint.scanner.adapters.v2.toUnifiedVersionInformation
import com.simprints.fingerprint.scanner.data.local.FirmwareLocalDataSource
import com.simprints.fingerprint.scanner.domain.ota.StmOtaStep
import com.simprints.fingerprint.scanner.domain.versions.ChipApiVersion
import com.simprints.fingerprint.scanner.domain.versions.ChipFirmwareVersion
import com.simprints.fingerprint.scanner.domain.versions.ScannerVersion
import com.simprints.fingerprint.scanner.exceptions.safe.OtaFailedException
import com.simprints.fingerprint.tools.doIfNotNull
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

    private var newFirmwareVersion: ChipFirmwareVersion? = null
    private var newApiVersion: ChipApiVersion? = null

    /**
     * Expects a connected scanner in root mode
     * If completes successfully, will finish with a connected scanner in root mode
     */
    fun performOtaSteps(scanner: Scanner, macAddress: String): Observable<StmOtaStep> =
        Observable.just<StmOtaStep>(StmOtaStep.EnteringOtaModeFirstTime)
            .concatWith(scanner.enterStmOtaMode().onErrorComplete() thenEmitStep StmOtaStep.ReconnectingAfterEnteringOtaMode)
            .concatWith(connectionHelper.reconnect(scanner, macAddress).addSmallDelay() thenEmitStep StmOtaStep.EnteringOtaModeSecondTime)
            .concatWith(scanner.enterStmOtaMode().addSmallDelay() thenEmitStep StmOtaStep.CommencingTransfer)
            .concatWith(scanner.startStmOta(firmwareLocalDataSource.loadStmFirmwareBytes()).map { StmOtaStep.TransferInProgress(it) })
            .concatWith(emitStep(StmOtaStep.ReconnectingAfterTransfer))
            .concatWith(connectionHelper.reconnect(scanner, macAddress).addSmallDelay() thenEmitStep StmOtaStep.EnteringMainMode)
            .concatWith(scanner.enterMainMode().addSmallDelay() thenEmitStep StmOtaStep.ValidatingNewFirmwareVersion)
            .concatWith(validateStmFirmwareVersion(scanner) thenEmitStep StmOtaStep.ReconnectingAfterValidating)
            .concatWith(connectionHelper.reconnect(scanner, macAddress).addSmallDelay() thenEmitStep StmOtaStep.UpdatingUnifiedVersionInformation)
            .concatWith(updateUnifiedVersionInformation(scanner))

    private fun Completable.addSmallDelay() = delay(1, TimeUnit.SECONDS, timeScheduler)

    private fun validateStmFirmwareVersion(scanner: Scanner): Completable =
        scanner.getStmFirmwareVersion().flatMapCompletable {
            val expectedFirmwareVersion = firmwareLocalDataSource.getAvailableScannerFirmwareVersions().stm
            val actualFirmwareVersion = it.toChipFirmwareVersion()
            if (expectedFirmwareVersion != actualFirmwareVersion) {
                Completable.error(OtaFailedException("STM OTA did not increment firmware version. Expected $expectedFirmwareVersion, but was $actualFirmwareVersion"))
            } else {
                newFirmwareVersion = it.toChipFirmwareVersion()
                newApiVersion = it.toChipApiVersion()
                Completable.complete()
            }
        }

    private fun updateUnifiedVersionInformation(scanner: Scanner): Completable =
        scanner.getVersionInformation().flatMapCompletable {
            doIfNotNull(newFirmwareVersion, newApiVersion) { newFirmwareVersion, newApiVersion ->
                val oldVersion = it.toScannerVersion()
                val newVersion = oldVersion.updatedWithStmVersion(newFirmwareVersion, newApiVersion)
                scanner.setVersionInformation(newVersion.toUnifiedVersionInformation())
            }
                ?: Completable.error(OtaFailedException("Was not able to determine the appropriate new unified version"))
        }

    private fun ScannerVersion.updatedWithStmVersion(stmFirmware: ChipFirmwareVersion, stmApi: ChipApiVersion) =
        copy(firmware = firmware.copy(stm = stmFirmware), api = api.copy(stm = stmApi))

    private fun emitStep(step: StmOtaStep) = Single.just(step)
    private infix fun Completable.thenEmitStep(step: StmOtaStep) = andThen(emitStep(step))
}
