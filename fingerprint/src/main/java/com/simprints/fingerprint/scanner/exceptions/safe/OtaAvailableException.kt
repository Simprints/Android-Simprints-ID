package com.simprints.fingerprint.scanner.exceptions.safe

import com.simprints.fingerprint.exceptions.safe.FingerprintSafeException
import com.simprints.fingerprint.scanner.domain.ota.AvailableOta

class OtaAvailableException(val availableOtas: List<AvailableOta>)
    : FingerprintSafeException("There are available OTAs: ${availableOtas.map { it.toString() }.reduce { acc, s -> "$acc, $s" }}") {
}
