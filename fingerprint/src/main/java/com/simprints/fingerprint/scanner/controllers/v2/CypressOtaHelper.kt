package com.simprints.fingerprint.scanner.controllers.v2

import com.simprints.fingerprint.scanner.adapters.v2.toChipApiVersion
import com.simprints.fingerprint.scanner.adapters.v2.toChipFirmwareVersion
import com.simprints.fingerprint.scanner.adapters.v2.toScannerVersion
import com.simprints.fingerprint.scanner.adapters.v2.toUnifiedVersionInformation
import com.simprints.fingerprint.scanner.data.FirmwareFileManager
import com.simprints.fingerprint.scanner.domain.ota.CypressOtaStep
import com.simprints.fingerprint.scanner.domain.versions.ChipApiVersion
import com.simprints.fingerprint.scanner.domain.versions.ChipFirmwareVersion
import com.simprints.fingerprint.scanner.domain.versions.ScannerVersion
import com.simprints.fingerprint.scanner.exceptions.safe.OtaFailedException
import com.simprints.fingerprint.tools.doIfNotNull
import com.simprints.fingerprintscanner.v2.scanner.Scanner
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

class CypressOtaHelper(private val connectionHelper: ConnectionHelper,
                       private val firmwareFileManager: FirmwareFileManager) {

    private var newFirmwareVersion: ChipFirmwareVersion? = null
    private var newApiVersion: ChipApiVersion? = null

    /**
     * Expects a connected scanner in root mode
     * If completes successfully, will finish with a connected scanner in root mode
     */
    fun performOtaSteps(scanner: Scanner, macAddress: String): Observable<CypressOtaStep> =
        Observable.just<CypressOtaStep>(CypressOtaStep.EnteringOtaMode)
            .concatWith(scanner.enterCypressOtaMode() thenEmitStep CypressOtaStep.CommencingTransfer)
            .concatWith(scanner.startCypressOta(firmwareFileManager.loadCypressFirmwareBytes()).map { CypressOtaStep.TransferInProgress(it) })
            .concatWith(emitStep(CypressOtaStep.ReconnectingAfterTransfer))
            .concatWith(connectionHelper.reconnect(scanner, macAddress) thenEmitStep CypressOtaStep.ValidatingNewFirmwareVersion)
            .concatWith(validateCypressFirmwareVersion(scanner) thenEmitStep CypressOtaStep.UpdatingUnifiedVersionInformation)
            .concatWith(updateUnifiedVersionInformation(scanner))

    private fun validateCypressFirmwareVersion(scanner: Scanner): Completable =
        scanner.getCypressFirmwareVersion().flatMapCompletable {
            val expectedFirmwareVersion = firmwareFileManager.getAvailableScannerFirmwareVersions().cypress
            val actualFirmwareVersion = it.toChipFirmwareVersion()
            if (expectedFirmwareVersion != actualFirmwareVersion) {
                Completable.error(OtaFailedException("Cypress OTA did not increment firmware version. Expected $expectedFirmwareVersion, but was $actualFirmwareVersion"))
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
                val newVersion = oldVersion.updatedWithCypressVersion(newFirmwareVersion, newApiVersion)
                scanner.setVersionInformation(newVersion.toUnifiedVersionInformation())
            }
                ?: Completable.error(OtaFailedException("Was not able to determine the appropriate new unified version"))
        }

    private fun ScannerVersion.updatedWithCypressVersion(cypressFirmware: ChipFirmwareVersion, cypressApi: ChipApiVersion) =
        copy(firmware = firmware.copy(cypress = cypressFirmware), api = api.copy(cypress = cypressApi))

    private fun emitStep(step: CypressOtaStep) = Single.just(step)
    private infix fun Completable.thenEmitStep(step: CypressOtaStep) = andThen(emitStep(step))
}
