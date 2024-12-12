package com.simprints.fingerprint.infra.scanner.helpers

import com.simprints.fingerprint.infra.scanner.data.local.FirmwareLocalDataSource
import com.simprints.fingerprint.infra.scanner.domain.ota.CypressOtaStep
import com.simprints.fingerprint.infra.scanner.domain.versions.ScannerVersion
import com.simprints.fingerprint.infra.scanner.exceptions.safe.OtaFailedException
import com.simprints.fingerprint.infra.scanner.v2.domain.root.models.CypressExtendedFirmwareVersion
import com.simprints.fingerprint.infra.scanner.v2.scanner.Scanner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private typealias CypressStep = FlowCollector<CypressOtaStep>

/**
 * This class handles Cypress (bluetooth) over the air firmware update, on the fingerprint scanner.
 * It establishes connection to a scanner, in root-mode, and loads the updated firmware bytes on
 * the connected Vero 2 scanner.
 *
 * @param connectionHelper  for connection operations on the scanner
 * @param firmwareLocalDataSource  for retrieving firmware bytes to be transferred for update
 */
internal class CypressOtaHelper @Inject constructor(
    private val connectionHelper: ConnectionHelper,
    private val firmwareLocalDataSource: FirmwareLocalDataSource,
) {
    private var newFirmwareVersion: CypressExtendedFirmwareVersion? = null

    /**
     * This function is responsible for performing Cypress firmware updates on Vero 2 Scanner, and
     * emits a sequence of each step [CypressOtaStep] being processed.
     *
     * If completes successfully, will finish with a connected scanner in root mode
     *
     * @param scanner the connected scanner expected to be in root mode
     * @param macAddress the scanner's mac address
     *
     * @return a flow of [CypressOtaStep] which represents the sequence of update steps being taken
     */
    fun performOtaSteps(
        scanner: Scanner,
        macAddress: String,
        firmwareVersion: String,
    ): Flow<CypressOtaStep> = flow {
        enterCypressOtaMode(scanner)
        transferFirmwareBytes(scanner, firmwareVersion)
        reconnectAfterTransfer(scanner, macAddress)
        validateRunningFirmwareVersion(scanner, firmwareVersion)
        updateFirmwareVersionInfo(scanner)
    }

    private suspend fun CypressStep.enterCypressOtaMode(scanner: Scanner) {
        emit(CypressOtaStep.EnteringOtaMode)
        scanner.enterCypressOtaMode()
        delayForOneSecond()
    }

    private suspend fun CypressStep.transferFirmwareBytes(
        scanner: Scanner,
        firmwareVersion: String,
    ) {
        emit(CypressOtaStep.CommencingTransfer)
        scanner
            .startCypressOta(firmwareLocalDataSource.loadCypressFirmwareBytes(firmwareVersion))
            .map { CypressOtaStep.TransferInProgress(it) }
            .collect { emit(it) }
    }

    private suspend fun CypressStep.reconnectAfterTransfer(
        scanner: Scanner,
        macAddress: String,
    ) {
        emit(CypressOtaStep.ReconnectingAfterTransfer)
        connectionHelper.reconnect(scanner, macAddress)
        delayForOneSecond()
    }

    private suspend fun CypressStep.validateRunningFirmwareVersion(
        scanner: Scanner,
        firmwareVersion: String,
    ) {
        emit(CypressOtaStep.ValidatingNewFirmwareVersion)
        validateCypressFirmwareVersion(scanner, firmwareVersion)
    }

    private suspend fun CypressStep.updateFirmwareVersionInfo(scanner: Scanner) {
        emit(CypressOtaStep.UpdatingUnifiedVersionInformation)
        updateUnifiedVersionInformation(scanner)
    }

    private suspend fun validateCypressFirmwareVersion(
        scanner: Scanner,
        firmwareVersion: String,
    ) = scanner.getCypressExtendedFirmwareVersion().let {
        val actualFirmwareVersion = it.versionAsString
        if (firmwareVersion != actualFirmwareVersion) {
            throw OtaFailedException(
                "Cypress OTA did not increment firmware version. Expected $firmwareVersion, but was $actualFirmwareVersion",
            )
        } else {
            newFirmwareVersion = it
        }
    }

    private suspend fun updateUnifiedVersionInformation(scanner: Scanner) = scanner.getVersionInformation().let {
        newFirmwareVersion?.let { newFirmwareVersion ->
            val oldVersion = it.toScannerVersion()
            val newVersion = oldVersion.updatedWithCypressVersion(newFirmwareVersion)

            scanner.setVersionInformation(newVersion.toExtendedVersionInformation())
        }
            ?: throw OtaFailedException("Was not able to determine the appropriate new unified version")
    }

    private fun ScannerVersion.updatedWithCypressVersion(cypressFirmware: CypressExtendedFirmwareVersion) =
        copy(firmware = firmware.copy(cypress = cypressFirmware.versionAsString))
}
