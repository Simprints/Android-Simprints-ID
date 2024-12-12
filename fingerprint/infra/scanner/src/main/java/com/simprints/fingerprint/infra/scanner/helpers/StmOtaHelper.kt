package com.simprints.fingerprint.infra.scanner.helpers

import com.simprints.fingerprint.infra.scanner.data.local.FirmwareLocalDataSource
import com.simprints.fingerprint.infra.scanner.domain.ota.StmOtaStep
import com.simprints.fingerprint.infra.scanner.domain.versions.ScannerVersion
import com.simprints.fingerprint.infra.scanner.exceptions.safe.OtaFailedException
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.models.StmExtendedFirmwareVersion
import com.simprints.fingerprint.infra.scanner.v2.scanner.Scanner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private typealias StmStep = FlowCollector<StmOtaStep>

/**
 * This class handles STM over the air update, on the fingerprint scanner.
 * It establishes connection to a scanner, in root-mode, and loads the updated firmware bytes on
 * the connected Vero 2 scanner.
 *
 * @param connectionHelper  for connection operations on the scanner
 * @param firmwareLocalDataSource  for retrieving firmware bytes to be transferred for update
 */
internal class StmOtaHelper @Inject constructor(
    private val connectionHelper: ConnectionHelper,
    private val firmwareLocalDataSource: FirmwareLocalDataSource,
) {
    private var newFirmwareVersion: StmExtendedFirmwareVersion? = null

    /**
     * This function is responsible for performing STM firmware updates on Vero 2 Scanner, and
     * emits a sequence of each step [StmOtaStep] being processed.
     *
     * If completes successfully, will finish with a connected scanner in root mode
     *
     * @param scanner the connected scanner expected to be in root mode
     * @param macAddress the scanner's mac address
     */
    fun performOtaSteps(
        scanner: Scanner,
        macAddress: String,
        firmwareVersion: String,
    ): Flow<StmOtaStep> = flow {
        // enter stm ota mode, which
        // will trigger scanner restart
        enterStmOtaMode(scanner)
        // reconnect with scanner after restart, because connection will be lost
        reconnectAfterEnteringStmOtaMode(scanner, macAddress)
        // enter stm mode after reconnect
        reEnterStmOtaMode(scanner)
        // transfer bytes which will trigger a
        // restart after transfer is successful
        transferFirmwareBytes(scanner, firmwareVersion)
        // reconnect with scanner after transfer,
        // because connection will be lost
        reconnectAfterTransfer(scanner, macAddress)
        // enter main-mode to check firmware version
        enterMainMode(scanner)
        // validate running firmware version
        validateRunningFirmwareVersion(scanner, firmwareVersion)
        // reconnect with scanner to enter root mode
        reconnectAfterValidatingVersion(scanner, macAddress)
        // update firmware version
        updateVersionInfo(scanner)
    }

    private suspend fun StmStep.enterStmOtaMode(scanner: Scanner) {
        emit(StmOtaStep.EnteringOtaModeFirstTime)
        // Ignore exception here, as we will reconnect after this step
        runCatching { scanner.enterStmOtaMode() }
    }

    private suspend fun StmStep.reconnectAfterEnteringStmOtaMode(
        scanner: Scanner,
        macAddress: String,
    ) {
        emit(StmOtaStep.ReconnectingAfterEnteringOtaMode)
        connectionHelper.reconnect(scanner, macAddress)
        delayForOneSecond()
    }

    private suspend fun StmStep.reEnterStmOtaMode(scanner: Scanner) {
        emit(StmOtaStep.EnteringOtaModeSecondTime)
        scanner.enterStmOtaMode()
        delayForOneSecond()
    }

    private suspend fun StmStep.transferFirmwareBytes(
        scanner: Scanner,
        firmwareVersion: String,
    ) {
        // transfer bytes and publish the transfer-progress
        emit(StmOtaStep.CommencingTransfer)
        scanner
            .startStmOta(firmwareLocalDataSource.loadStmFirmwareBytes(firmwareVersion))
            .map { StmOtaStep.TransferInProgress(it) }
            .collect { emit(it) }
    }

    private suspend fun StmStep.reconnectAfterTransfer(
        scanner: Scanner,
        macAddress: String,
    ) {
        emit(StmOtaStep.ReconnectingAfterTransfer)
        connectionHelper.reconnect(scanner, macAddress)
        delayForOneSecond()
    }

    private suspend fun StmStep.enterMainMode(scanner: Scanner) {
        emit(StmOtaStep.EnteringMainMode)
        scanner.enterMainMode()
        delayForOneSecond()
    }

    private suspend fun StmStep.validateRunningFirmwareVersion(
        scanner: Scanner,
        firmwareVersion: String,
    ) {
        emit(StmOtaStep.ValidatingNewFirmwareVersion)
        validateStmFirmwareVersion(scanner, firmwareVersion)
    }

    private suspend fun StmStep.reconnectAfterValidatingVersion(
        scanner: Scanner,
        macAddress: String,
    ) {
        emit(StmOtaStep.ReconnectingAfterValidating)
        connectionHelper.reconnect(scanner, macAddress)
        delayForOneSecond()
    }

    private suspend fun StmStep.updateVersionInfo(scanner: Scanner) {
        emit(StmOtaStep.UpdatingUnifiedVersionInformation)
        updateUnifiedVersionInformation(scanner)
    }

    private suspend fun validateStmFirmwareVersion(
        scanner: Scanner,
        firmwareVersion: String,
    ) {
        val actualFirmwareVersion = scanner.getStmFirmwareVersion()
        if (firmwareVersion != actualFirmwareVersion.versionAsString) {
            throw OtaFailedException(
                "STM OTA did not increment firmware version. Expected $firmwareVersion, but was $actualFirmwareVersion",
            )
        } else {
            newFirmwareVersion = actualFirmwareVersion
        }
    }

    private suspend fun updateUnifiedVersionInformation(scanner: Scanner) {
        val oldVersion = scanner.getVersionInformation().toScannerVersion()
        newFirmwareVersion?.let { newFirmwareVersion ->
            val newVersion = oldVersion.updatedWithStmVersion(newFirmwareVersion)
            scanner.setVersionInformation(newVersion.toExtendedVersionInformation())
        }
            ?: throw OtaFailedException("Was not able to determine the appropriate new unified version")
    }

    private fun ScannerVersion.updatedWithStmVersion(stmFirmware: StmExtendedFirmwareVersion) =
        copy(firmware = firmware.copy(stm = stmFirmware.versionAsString))
}
