package com.simprints.fingerprint.scanner.controllers.v2

import com.simprints.fingerprint.scanner.adapters.v2.toChipApiVersion
import com.simprints.fingerprint.scanner.adapters.v2.toChipFirmwareVersion
import com.simprints.fingerprint.scanner.adapters.v2.toScannerVersion
import com.simprints.fingerprint.scanner.adapters.v2.toUnifiedVersionInformation
import com.simprints.fingerprint.scanner.data.FirmwareFileManager
import com.simprints.fingerprint.scanner.domain.ota.Un20OtaStep
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

class Un20OtaHelper(private val connectionHelper: ConnectionHelper,
                    private val firmwareFileManager: FirmwareFileManager,
                    private val timeScheduler: Scheduler = Schedulers.io()) {

    private var newFirmwareVersion: ChipFirmwareVersion? = null
    private var newApiVersion: ChipApiVersion? = null

    /**
     * Expects a connected scanner in root mode
     * If completes successfully, will finish with a connected scanner in root mode
     */
    fun performOtaSteps(scanner: Scanner, macAddress: String): Observable<Un20OtaStep> =
        Observable.just<Un20OtaStep>(Un20OtaStep.EnteringMainMode)
            .concatWith(scanner.enterMainMode() thenEmitStep Un20OtaStep.TurningOnUn20BeforeTransfer)
            .concatWith(scanner.turnUn20OnAndAwaitStateChangeEvent() thenEmitStep Un20OtaStep.CommencingTransfer)
            .concatWith(scanner.startUn20Ota(firmwareFileManager.loadUn20FirmwareBytes()).map { Un20OtaStep.TransferInProgress(it) })
            .concatWith(emitStep(Un20OtaStep.AwaitingCacheCommit))
            .concatWith(waitCacheCommitTime() thenEmitStep Un20OtaStep.TurningOffUn20AfterTransfer)
            .concatWith(scanner.turnUn20OffAndAwaitStateChangeEvent() thenEmitStep Un20OtaStep.TurningOnUn20AfterTransfer)
            .concatWith(scanner.turnUn20OnAndAwaitStateChangeEvent() thenEmitStep Un20OtaStep.ValidatingNewFirmwareVersion)
            .concatWith(validateUn20FirmwareVersion(scanner) thenEmitStep Un20OtaStep.ReconnectingAfterValidating)
            .concatWith(connectionHelper.reconnect(scanner, macAddress) thenEmitStep Un20OtaStep.UpdatingUnifiedVersionInformation)
            .concatWith(updateUnifiedVersionInformation(scanner))

    private fun waitCacheCommitTime(): Completable =
        Completable.timer(CACHE_COMMIT_TIME, TimeUnit.MILLISECONDS, timeScheduler)

    private fun validateUn20FirmwareVersion(scanner: Scanner): Completable =
        scanner.getUn20AppVersion().flatMapCompletable {
            val expectedFirmwareVersion = firmwareFileManager.getAvailableScannerFirmwareVersions().un20
            val actualFirmwareVersion = it.toChipFirmwareVersion()
            if (expectedFirmwareVersion != actualFirmwareVersion) {
                Completable.error(OtaFailedException("UN20 OTA did not increment firmware version. Expected $expectedFirmwareVersion, but was $actualFirmwareVersion"))
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
                val newVersion = oldVersion.updatedWithUn20Version(newFirmwareVersion, newApiVersion)
                scanner.setVersionInformation(newVersion.toUnifiedVersionInformation())
            }
                ?: Completable.error(OtaFailedException("Was not able to determine the appropriate new unified version"))
        }

    private fun ScannerVersion.updatedWithUn20Version(un20Firmware: ChipFirmwareVersion, un20Api: ChipApiVersion) =
        copy(firmware = firmware.copy(un20 = un20Firmware), api = api.copy(un20 = un20Api))

    private fun emitStep(step: Un20OtaStep) = Single.just(step)
    private infix fun Completable.thenEmitStep(step: Un20OtaStep) = andThen(emitStep(step))

    companion object {
        const val CACHE_COMMIT_TIME = 10000L // milliseconds
    }
}
