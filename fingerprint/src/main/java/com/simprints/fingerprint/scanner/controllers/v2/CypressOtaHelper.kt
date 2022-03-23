package com.simprints.fingerprint.scanner.controllers.v2

import com.simprints.fingerprint.scanner.adapters.v2.toExtendedVersionInformation
import com.simprints.fingerprint.scanner.adapters.v2.toScannerVersion
import com.simprints.fingerprint.scanner.data.local.FirmwareLocalDataSource
import com.simprints.fingerprint.scanner.domain.ota.CypressOtaStep
import com.simprints.fingerprint.scanner.domain.versions.ScannerVersion
import com.simprints.fingerprint.scanner.exceptions.safe.OtaFailedException
import com.simprints.fingerprintscanner.v2.domain.root.models.CypressExtendedFirmwareVersion
import com.simprints.fingerprintscanner.v2.scanner.Scanner
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class CypressOtaHelper(private val connectionHelper: ConnectionHelper,
                       private val firmwareLocalDataSource: FirmwareLocalDataSource,
                       private val timeScheduler: Scheduler = Schedulers.io()) {

    private var newFirmwareVersion: CypressExtendedFirmwareVersion? = null

    /**
     * Expects a connected scanner in root mode
     * If completes successfully, will finish with a connected scanner in root mode
     */
    fun performOtaSteps(scanner: Scanner, macAddress: String, firmwareVersion: String): Observable<CypressOtaStep> =
        Observable.just<CypressOtaStep>(CypressOtaStep.EnteringOtaMode)
            .concatWith(scanner.enterCypressOtaMode().addSmallDelay() thenEmitStep CypressOtaStep.CommencingTransfer)
            .concatWith(scanner.startCypressOta(firmwareLocalDataSource.loadCypressFirmwareBytes(firmwareVersion)).map { CypressOtaStep.TransferInProgress(it) })
            .concatWith(emitStep(CypressOtaStep.ReconnectingAfterTransfer))
            .concatWith(connectionHelper.reconnect(scanner, macAddress).addSmallDelay() thenEmitStep CypressOtaStep.ValidatingNewFirmwareVersion)
            .concatWith(validateCypressFirmwareVersion(firmwareVersion, scanner) thenEmitStep CypressOtaStep.UpdatingUnifiedVersionInformation)
            .concatWith(updateUnifiedVersionInformation(scanner))

    private fun Completable.addSmallDelay() = delay(1, TimeUnit.SECONDS, timeScheduler)

    private fun validateCypressFirmwareVersion(firmwareVersion: String, scanner: Scanner): Completable =
        scanner.getCypressExtendedFirmwareVersion().flatMapCompletable {
            val actualFirmwareVersion = it.versionAsString
            if (firmwareVersion != actualFirmwareVersion) {
                Completable.error(OtaFailedException("Cypress OTA did not increment firmware version. Expected $firmwareVersion, but was $actualFirmwareVersion"))
            } else {
                newFirmwareVersion = it
                Completable.complete()
            }
        }

    private fun updateUnifiedVersionInformation(scanner: Scanner): Completable =
        scanner.getVersionInformation().flatMapCompletable {
            newFirmwareVersion?.let { newFirmwareVersion ->
                val oldVersion = it.toScannerVersion()
                val newVersion = oldVersion.updatedWithCypressVersion(newFirmwareVersion)

                scanner.setVersionInformation(newVersion.toExtendedVersionInformation())
            } ?: Completable.error(OtaFailedException("Was not able to determine the appropriate new unified version"))
        }

    private fun ScannerVersion.updatedWithCypressVersion(cypressFirmware: CypressExtendedFirmwareVersion) =
        copy(firmware = firmware.copy(cypress = cypressFirmware.versionAsString))

    private fun emitStep(step: CypressOtaStep) = Single.just(step)
    private infix fun Completable.thenEmitStep(step: CypressOtaStep) = andThen(emitStep(step))
}
