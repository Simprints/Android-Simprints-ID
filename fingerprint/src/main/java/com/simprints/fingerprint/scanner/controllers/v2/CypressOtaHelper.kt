package com.simprints.fingerprint.scanner.controllers.v2

import com.simprints.fingerprint.scanner.adapters.v2.toExtendedVersionInformation
import com.simprints.fingerprint.scanner.adapters.v2.toScannerVersion
import com.simprints.fingerprint.scanner.data.local.FirmwareLocalDataSource
import com.simprints.fingerprint.scanner.domain.ota.CypressOtaStep
import com.simprints.fingerprint.scanner.domain.versions.ScannerVersion
import com.simprints.fingerprint.scanner.exceptions.safe.OtaFailedException
import com.simprints.fingerprint.tools.extensions.delayForOneSecond
import com.simprints.fingerprintscanner.v2.domain.root.models.CypressExtendedFirmwareVersion
import com.simprints.fingerprintscanner.v2.scanner.Scanner
import io.reactivex.Completable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.rx2.asFlow
import kotlinx.coroutines.rx2.await


private typealias CypressStep = FlowCollector<CypressOtaStep>

/**
 * This class handles Cypress (bluetooth) over the air firmware update, on the fingerprint scanner.
 * It establishes connection to a scanner, in root-mode, and loads the updated firmware bytes on
 * the connected Vero 2 scanner.
 *
 * @param connectionHelper  for connection operations on the scanner
 * @param firmwareLocalDataSource  for retrieving firmware bytes to be transferred for update
 */
class CypressOtaHelper(private val connectionHelper: ConnectionHelper,
                       private val firmwareLocalDataSource: FirmwareLocalDataSource) {

    private var newFirmwareVersion: CypressExtendedFirmwareVersion? = null

    /**
     * This function is responsible for performing Cypress firmware updates on Vero 2 Scanner, and
     * emits a sequence of each step [CypressOtaStep] being processed.
     *
     * If completes successfully, will finish with a connected scanner in root mode
     *
     * @param scanner the connected scanner expected to be in root mode
     * @param macAddress the scanner's macAddress
     *
     */
    fun performOtaSteps(scanner: Scanner, macAddress: String, firmwareVersion: String): Flow<CypressOtaStep> = flow {
        enterCypressOtaMode(scanner)
        transferFirmwareBytes(scanner,firmwareVersion)
        reconnectAfterTransfer(scanner, macAddress)
        validateRunningFirmwareVersion(scanner,firmwareVersion)
        updateFirmwareVersionInfo(scanner)
    }

    private suspend fun CypressStep.enterCypressOtaMode(scanner: Scanner) {
        emit(CypressOtaStep.EnteringOtaMode)
        scanner.enterCypressOtaMode().await()
        delayForOneSecond()
    }

    private suspend fun CypressStep.transferFirmwareBytes(scanner: Scanner, firmwareVersion: String) {
        emit(CypressOtaStep.CommencingTransfer)
        scanner.startCypressOta(firmwareLocalDataSource.loadCypressFirmwareBytes(firmwareVersion))
            .map { CypressOtaStep.TransferInProgress(it) }
            .asFlow()
            .collect { emit(it) }
    }

    private suspend fun CypressStep.reconnectAfterTransfer(scanner: Scanner, macAddress: String) {
        emit(CypressOtaStep.ReconnectingAfterTransfer)
        connectionHelper.reconnect(scanner, macAddress)
        delayForOneSecond()
    }

    private suspend fun CypressStep.validateRunningFirmwareVersion(scanner: Scanner,firmwareVersion: String) {
        emit(CypressOtaStep.ValidatingNewFirmwareVersion)
        validateCypressFirmwareVersion(scanner,firmwareVersion)
    }

    private suspend fun CypressStep.updateFirmwareVersionInfo(scanner: Scanner) {
        emit(CypressOtaStep.UpdatingUnifiedVersionInformation)
        updateUnifiedVersionInformation(scanner)
    }

    private suspend fun validateCypressFirmwareVersion(scanner: Scanner, firmwareVersion: String) =
        scanner.getCypressExtendedFirmwareVersion().flatMapCompletable {
            val actualFirmwareVersion = it.versionAsString
            if (firmwareVersion != actualFirmwareVersion) {
                Completable.error(OtaFailedException("Cypress OTA did not increment firmware version. Expected $firmwareVersion, but was $actualFirmwareVersion"))
            } else {
                newFirmwareVersion = it
                Completable.complete()
            }
        }.await()

    private suspend fun updateUnifiedVersionInformation(scanner: Scanner) =
        scanner.getVersionInformation().flatMapCompletable {
            newFirmwareVersion?.let { newFirmwareVersion ->
                val oldVersion = it.toScannerVersion()
                val newVersion = oldVersion.updatedWithCypressVersion(newFirmwareVersion)

                scanner.setVersionInformation(newVersion.toExtendedVersionInformation())
            } ?: Completable.error(OtaFailedException("Was not able to determine the appropriate new unified version"))
        }.await()

    private fun ScannerVersion.updatedWithCypressVersion(cypressFirmware: CypressExtendedFirmwareVersion) =
        copy(firmware = firmware.copy(cypress = cypressFirmware.versionAsString))

}
