package com.simprints.fingerprint.scanner.controllers.v2

import com.simprints.fingerprint.exceptions.safe.FingerprintSafeException
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.models.Un20AppVersion
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.models.StmFirmwareVersion
import com.simprints.fingerprintscanner.v2.domain.root.models.CypressFirmwareVersion
import com.simprints.fingerprintscanner.v2.domain.root.models.UnifiedVersionInformation
import com.simprints.fingerprintscanner.v2.scanner.Scanner
import io.reactivex.Completable
import io.reactivex.Single
import java.util.concurrent.TimeUnit

class ScannerInitialSetupHelper {

    private lateinit var versionInformation: UnifiedVersionInformation

    fun setupScanner(scanner: Scanner, availableVersions: UnifiedVersionInformation?): Single<UnifiedVersionInformation> =
        Completable.complete()
            .delay(100, TimeUnit.MILLISECONDS) // Speculatively needed
            .andThen(scanner.getVersionInformation())
            .doOnSuccess { versionInformation = it }
            .ifAvailableOtasThenThrow(availableVersions)
            .andThen(scanner.enterMainMode())
            .delay(100, TimeUnit.MILLISECONDS) // Speculatively needed
            .andThen(Single.defer { Single.just(versionInformation) })

    private fun Single<UnifiedVersionInformation>.ifAvailableOtasThenThrow(availableVersions: UnifiedVersionInformation?): Completable =
        flatMapCompletable { scannerVersions ->
            val availableOtas = availableVersions?.let { determineAvailableOtas(scannerVersions, it) } ?: emptyList()
            if (availableOtas.isEmpty()) {
                Completable.complete()
            } else {
                Completable.error(OtaAvailableException(scannerVersions, availableOtas))
            }
        }

    private fun determineAvailableOtas(scannerVersions: UnifiedVersionInformation, availableVersions: UnifiedVersionInformation): List<AvailableOta> {
        val canCypressOta = scannerVersions.cypressFirmwareVersion.toInt() < availableVersions.cypressFirmwareVersion.toInt()
        val canStmOta = scannerVersions.stmFirmwareVersion.toInt() < availableVersions.stmFirmwareVersion.toInt()
        val canUn20Ota = scannerVersions.un20AppVersion.toInt() < availableVersions.un20AppVersion.toInt()
        return listOfNotNull(
            if (canCypressOta) AvailableOta.CYPRESS else null,
            if (canStmOta) AvailableOta.STM else null,
            if (canUn20Ota) AvailableOta.UN20 else null
        )
    }

    private fun CypressFirmwareVersion.toInt() =
        firmwareMajorVersion * (2 shl 16) + firmwareMinorVersion

    private fun StmFirmwareVersion.toInt() =
        firmwareMajorVersion * (2 shl 16) + firmwareMinorVersion

    private fun Un20AppVersion.toInt() =
        firmwareMajorVersion * (2 shl 16) + firmwareMinorVersion
}

class OtaAvailableException(val scannerVersion: UnifiedVersionInformation, val availableOtas: List<AvailableOta>)
    : FingerprintSafeException("There are available OTAs: ${availableOtas.map { it.toString() }.reduce { acc, s -> "$acc, $s" }}") {
}

enum class AvailableOta {
    CYPRESS,
    STM,
    UN20
}
