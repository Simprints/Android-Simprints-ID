package com.simprints.fingerprint.scanner.domain.ota

import androidx.annotation.Keep
import com.simprints.fingerprint.scanner.domain.ota.OtaRecoveryStrategy.*

/**
 * This enum class represents the different ways to recover from an issue that occurs during an
 * Over The Air update.
 *
 * - [HARD_RESET]  the user will be instructed to restart the Vero
 * - [SOFT_RESET]  this will handle reset in the background
 * - [SOFT_RESET_AFTER_DELAY]  this will handle reset in the background after some specified delay in milliseconds
 */
@Keep
enum class OtaRecoveryStrategy {
    HARD_RESET,
    SOFT_RESET,
    SOFT_RESET_AFTER_DELAY;

    companion object {
        const val DELAY_TIME_MS = 10000L
    }
}
