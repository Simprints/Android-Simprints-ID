package com.simprints.fingerprint.infra.scanner.exceptions.safe

import com.simprints.fingerprint.infra.scanner.domain.ota.AvailableOta

class OtaAvailableException(
    val availableOtas: List<AvailableOta>,
) : ScannerSafeException(
        "There are available OTAs: ${availableOtas.map { it.toString() }.reduce { acc, s -> "$acc, $s" }}",
    )
