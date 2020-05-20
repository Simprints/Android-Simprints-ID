package com.simprints.fingerprint.scanner.controllers.v2

import com.simprints.fingerprint.scanner.adapters.v2.toScannerVersion
import com.simprints.fingerprint.scanner.domain.AvailableOta
import com.simprints.fingerprint.scanner.domain.versions.ScannerFirmwareVersions
import com.simprints.fingerprint.scanner.domain.versions.ScannerVersion
import com.simprints.fingerprint.scanner.exceptions.safe.OtaAvailableException
import com.simprints.fingerprintscanner.v2.scanner.Scanner
import io.reactivex.Completable
import io.reactivex.Single
import java.util.concurrent.TimeUnit

class ScannerInitialSetupHelper {

    fun setupScanner(scanner: Scanner, availableVersions: ScannerVersion?, withScannerVersion: (ScannerVersion) -> Unit): Completable =
        Completable.complete()
            .delay(100, TimeUnit.MILLISECONDS) // Speculatively needed
            .andThen(scanner.getVersionInformation())
            .map { unifiedVersion -> unifiedVersion.toScannerVersion().also { withScannerVersion(it) } }
            .ifAvailableOtasThenThrow(availableVersions)
            .andThen(scanner.enterMainMode())
            .delay(100, TimeUnit.MILLISECONDS) // Speculatively needed

    private fun Single<ScannerVersion>.ifAvailableOtasThenThrow(availableVersions: ScannerVersion?): Completable =
        flatMapCompletable { scannerVersions ->
            val availableOtas = availableVersions?.firmware?.let { determineAvailableOtas(scannerVersions.firmware, it) }
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
