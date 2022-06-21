package com.simprints.fingerprint.scanner.controllers.v2

import com.simprints.fingerprint.scanner.adapters.v2.toExtendedVersionInformation
import com.simprints.fingerprint.scanner.adapters.v2.toScannerVersion
import com.simprints.fingerprint.scanner.data.local.FirmwareLocalDataSource
import com.simprints.fingerprint.scanner.domain.ota.Un20OtaStep
import com.simprints.fingerprint.scanner.domain.versions.ScannerVersion
import com.simprints.fingerprint.scanner.exceptions.safe.OtaFailedException
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.models.Un20ExtendedAppVersion
import com.simprints.fingerprintscanner.v2.scanner.Scanner
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class Un20OtaHelper(private val connectionHelper: ConnectionHelper,
                    private val firmwareLocalDataSource: FirmwareLocalDataSource,
                    private val timeScheduler: Scheduler = Schedulers.io()) {

    private var newFirmwareVersion: Un20ExtendedAppVersion? = null
    /**
     * Expects a connected scanner in root mode
     * If completes successfully, will finish with a connected scanner in root mode
     */
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
            .concatWith(connectionHelper.reconnect(scanner, macAddress).addSmallDelay() thenEmitStep Un20OtaStep.UpdatingUnifiedVersionInformation)
            .concatWith(updateUnifiedVersionInformation(scanner))

    private fun Completable.addSmallDelay() = delay(1, TimeUnit.SECONDS, timeScheduler)

    private fun waitCacheCommitTime(): Completable =
        Completable.timer(CACHE_COMMIT_TIME, TimeUnit.MILLISECONDS, timeScheduler)

    private fun validateUn20FirmwareVersion(firmwareVersion: String, scanner: Scanner): Completable =
        scanner.getUn20AppVersion().flatMapCompletable {
            val actualFirmwareVersion = it.versionAsString
            if (firmwareVersion != actualFirmwareVersion) {
                Completable.error(OtaFailedException("UN20 OTA did not increment firmware version. Expected $firmwareVersion, but was $actualFirmwareVersion"))
            } else {
                newFirmwareVersion = it
                Completable.complete()
            }
        }

    private fun updateUnifiedVersionInformation(scanner: Scanner): Completable =
        scanner.getVersionInformation().flatMapCompletable {
            newFirmwareVersion?.let { newFirmwareVersion ->
                val oldVersion = it.toScannerVersion()
                val newVersion = oldVersion.updatedWithUn20Version(newFirmwareVersion)
                scanner.setVersionInformation(newVersion.toExtendedVersionInformation())
            } ?: Completable.error(OtaFailedException("Was not able to determine the appropriate new unified version"))
        }

    private fun ScannerVersion.updatedWithUn20Version(un20Firmware: Un20ExtendedAppVersion) =
        copy(firmware = firmware.copy(un20 = un20Firmware.versionAsString))

    private fun emitStep(step: Un20OtaStep) = Single.just(step)
    private infix fun Completable.thenEmitStep(step: Un20OtaStep) = andThen(emitStep(step))

    companion object {
        const val CACHE_COMMIT_TIME = 10000L // milliseconds
    }
}
