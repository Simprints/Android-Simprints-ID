package com.simprints.fingerprint.scanner.controllers.v2

import com.simprints.fingerprint.scanner.adapters.v2.toScannerVersion
import com.simprints.fingerprint.scanner.data.FirmwareFileManager
import com.simprints.fingerprint.scanner.domain.ota.AvailableOta
import com.simprints.fingerprint.scanner.domain.versions.ScannerFirmwareVersions
import com.simprints.fingerprint.scanner.domain.versions.ScannerVersion
import com.simprints.fingerprint.scanner.exceptions.safe.OtaAvailableException
import com.simprints.fingerprintscanner.v2.scanner.Scanner
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class ScannerInitialSetupHelper(private val firmwareFileManager: FirmwareFileManager,
                                private val timeScheduler: Scheduler = Schedulers.io()) {

    fun setupScannerWithOtaCheck(scanner: Scanner, withScannerVersion: (ScannerVersion) -> Unit): Completable =
        Completable.complete()
            .delay(100, TimeUnit.MILLISECONDS, timeScheduler) // Speculatively needed
            .andThen(scanner.getVersionInformation())
            .map { unifiedVersion -> unifiedVersion.toScannerVersion().also { withScannerVersion(it) } }
            .ifAvailableOtasThenThrow(firmwareFileManager.getAvailableScannerFirmwareVersions())
            .andThen(scanner.enterMainMode())
            .delay(100, TimeUnit.MILLISECONDS, timeScheduler) // Speculatively needed

    private fun Single<ScannerVersion>.ifAvailableOtasThenThrow(availableVersions: ScannerFirmwareVersions?): Completable =
        flatMapCompletable { scannerVersions ->
            val availableOtas = availableVersions?.let { determineAvailableOtas(scannerVersions.firmware, it) }
                ?: emptyList()
            if (availableOtas.isEmpty()) {
                Completable.complete()
            } else {
                Completable.error(OtaAvailableException(availableOtas))
            }
        }

    private fun determineAvailableOtas(current: ScannerFirmwareVersions, available: ScannerFirmwareVersions): List<AvailableOta> =
        listOfNotNull(
            if (current.cypress < available.cypress) AvailableOta.CYPRESS else null,
            if (current.stm < available.stm) AvailableOta.STM else null,
            if (current.un20 < available.un20) AvailableOta.UN20 else null
        )
}
