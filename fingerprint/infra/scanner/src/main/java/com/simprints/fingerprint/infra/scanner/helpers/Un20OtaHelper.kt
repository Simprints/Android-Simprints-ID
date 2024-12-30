package com.simprints.fingerprint.infra.scanner.helpers

import com.simprints.fingerprint.infra.scanner.data.local.FirmwareLocalDataSource
import com.simprints.fingerprint.infra.scanner.domain.ota.Un20OtaStep
import com.simprints.fingerprint.infra.scanner.domain.versions.ScannerVersion
import com.simprints.fingerprint.infra.scanner.exceptions.safe.OtaFailedException
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.Un20ExtendedAppVersion
import com.simprints.fingerprint.infra.scanner.v2.scanner.Scanner
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private typealias Un20Step = FlowCollector<Un20OtaStep>

/**
 * This class handles Un20 over the air firmware update, on the fingerprint scanner.
 * It establishes connection to a scanner, in root-mode, and loads the updated firmware bytes on
 * the connected Vero 2 scanner.
 *
 * @param connectionHelper  for connection operations on the scanner
 * @param firmwareLocalDataSource  for retrieving firmware bytes to be transferred for update
 */
internal class Un20OtaHelper @Inject constructor(
    private val connectionHelper: ConnectionHelper,
    private val firmwareLocalDataSource: FirmwareLocalDataSource,
) {
    private var newFirmwareVersion: Un20ExtendedAppVersion? = null

    /**
     * This function is responsible for performing Un20 firmware updates on Vero 2 Scanner, and
     * emits a sequence of each step [Un20OtaStep] being processed.
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
    ): Flow<Un20OtaStep> = flow {
        enterMainMode(scanner)
        turnOnUn20BeforeTransfer(scanner)
        transferFirmwareBytes(scanner, firmwareVersion)
        awaitCacheCommit()
        turnOffUn20AfterTransfer(scanner)
        turnOnUn20AfterTransfer(scanner)
        validateRunningFirmwareVersion(scanner, firmwareVersion)
        reconnectScannerAfterValidating(scanner, macAddress)
        updateFirmwareVersion(scanner)
    }

    private suspend fun Un20Step.enterMainMode(scanner: Scanner) {
        emit(Un20OtaStep.EnteringMainMode)
        scanner.enterMainMode()
        delayForOneSecond()
    }

    private suspend fun Un20Step.turnOnUn20BeforeTransfer(scanner: Scanner) {
        emit(Un20OtaStep.TurningOnUn20BeforeTransfer)
        scanner.turnUn20On()
    }

    private suspend fun Un20Step.transferFirmwareBytes(
        scanner: Scanner,
        firmwareVersion: String,
    ) {
        emit(Un20OtaStep.CommencingTransfer)
        scanner
            .startUn20Ota(firmwareLocalDataSource.loadUn20FirmwareBytes(firmwareVersion))
            .map { Un20OtaStep.TransferInProgress(it) }
            .collect { emit(it) }
    }

    private suspend fun Un20Step.awaitCacheCommit() {
        emit(Un20OtaStep.AwaitingCacheCommit)
        waitCacheCommitTime()
    }

    private suspend fun Un20Step.turnOffUn20AfterTransfer(scanner: Scanner) {
        emit(Un20OtaStep.TurningOffUn20AfterTransfer)
        scanner.turnUn20Off()
        delayForOneSecond()
    }

    private suspend fun Un20Step.turnOnUn20AfterTransfer(scanner: Scanner) {
        emit(Un20OtaStep.TurningOnUn20AfterTransfer)
        scanner.turnUn20On()
    }

    private suspend fun Un20Step.validateRunningFirmwareVersion(
        scanner: Scanner,
        firmwareVersion: String,
    ) {
        emit(Un20OtaStep.ValidatingNewFirmwareVersion)
        validateUn20FirmwareVersion(scanner, firmwareVersion)
    }

    private suspend fun Un20Step.reconnectScannerAfterValidating(
        scanner: Scanner,
        macAddress: String,
    ) {
        emit(Un20OtaStep.ReconnectingAfterValidating)
        connectionHelper.reconnect(scanner, macAddress)
        delayForOneSecond()
    }

    private suspend fun Un20Step.updateFirmwareVersion(scanner: Scanner) {
        emit(Un20OtaStep.UpdatingUnifiedVersionInformation)
        updateUnifiedVersionInformation(scanner)
    }

    private suspend fun validateUn20FirmwareVersion(
        scanner: Scanner,
        firmwareVersion: String,
    ) = scanner.getUn20AppVersion().let {
        val actualFirmwareVersion = it.versionAsString
        if (firmwareVersion != actualFirmwareVersion) {
            throw OtaFailedException(
                "UN20 OTA did not increment firmware version. Expected $firmwareVersion, but was $actualFirmwareVersion",
            )
        } else {
            newFirmwareVersion = it
        }
    }

    private suspend fun updateUnifiedVersionInformation(scanner: Scanner) = scanner.getVersionInformation().let {
        newFirmwareVersion?.let { newFirmwareVersion ->
            val oldVersion = it.toScannerVersion()
            val newVersion = oldVersion.updatedWithUn20Version(newFirmwareVersion)
            scanner.setVersionInformation(newVersion.toExtendedVersionInformation())
        }
            ?: throw OtaFailedException("Was not able to determine the appropriate new unified version")
    }

    private fun ScannerVersion.updatedWithUn20Version(un20Firmware: Un20ExtendedAppVersion) =
        copy(firmware = firmware.copy(un20 = un20Firmware.versionAsString))

    private suspend fun waitCacheCommitTime() = delay(CACHE_COMMIT_TIME)

    companion object {
        const val CACHE_COMMIT_TIME = 10000L // milliseconds
    }
}
