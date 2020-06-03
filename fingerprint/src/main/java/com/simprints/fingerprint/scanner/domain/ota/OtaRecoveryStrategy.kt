package com.simprints.fingerprint.scanner.domain.ota

import androidx.annotation.Keep

@Keep
enum class OtaRecoveryStrategy {
    HARD_RESET,
    SOFT_RESET,
    SOFT_RESET_AFTER_DELAY;

    companion object {
        const val DELAY_TIME_MS = 10000L
    }
}
