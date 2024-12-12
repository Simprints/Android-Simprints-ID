package com.simprints.fingerprint.infra.scanner.domain.ota

import androidx.annotation.Keep

/**
 * This enum class represents the different firmware that can be updated on the vero 2 scanner
 */
@Keep
enum class AvailableOta {
    CYPRESS,
    STM,
    UN20,
}
