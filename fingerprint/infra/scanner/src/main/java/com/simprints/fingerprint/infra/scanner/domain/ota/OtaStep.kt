package com.simprints.fingerprint.infra.scanner.domain.ota

import com.simprints.fingerprint.infra.scanner.domain.ota.Un20OtaStep.AwaitingCacheCommit

/**
 * This class represents the ongoing step in an Over the Air update.
 *
 * @property totalProgress  the current progress percentage based on the current step in relation to
 *                          the total number of steps required for a particular firmware update.
 *
 * @property recoveryStrategy  the chosen OTA recovery strategy for a particular step
 *
 */
sealed class OtaStep(
    val totalProgress: Float,
    val recoveryStrategy: OtaRecoveryStrategy,
)

/**
 * This class represents the different steps that occur during a Cypress (bluetooth) firmware over
 * the air update.
 */
sealed class CypressOtaStep(
    totalProgress: Float,
    recoveryStrategy: OtaRecoveryStrategy = OtaRecoveryStrategy.SOFT_RESET,
) : OtaStep(totalProgress, recoveryStrategy) {
    data object EnteringOtaMode : CypressOtaStep(0.00f)

    data object CommencingTransfer : CypressOtaStep(0.05f)

    data class TransferInProgress(
        val otaProgress: Float,
    ) : CypressOtaStep(otaProgress.mapProgress(0.05f, 0.85f))

    data object ReconnectingAfterTransfer : CypressOtaStep(0.85f)

    data object ValidatingNewFirmwareVersion : CypressOtaStep(0.90f)

    data object UpdatingUnifiedVersionInformation : CypressOtaStep(0.95f)
}

/**
 * This class represents the different steps that occur during an STM firmware update
 *
 * NOTE: A quirk of the STM bootloader causes a disconnect upon first entering STM OTA mode, hence we
 * need to reconnect and enter STM OTA mode a second time.
 */
sealed class StmOtaStep(
    totalProgress: Float,
    recoveryStrategy: OtaRecoveryStrategy = OtaRecoveryStrategy.HARD_RESET,
) : OtaStep(totalProgress, recoveryStrategy) {
    data object EnteringOtaModeFirstTime : StmOtaStep(0.00f)

    data object ReconnectingAfterEnteringOtaMode : StmOtaStep(0.05f)

    data object EnteringOtaModeSecondTime : StmOtaStep(0.08f)

    data object CommencingTransfer : StmOtaStep(0.10f)

    data class TransferInProgress(
        val otaProgress: Float,
    ) : StmOtaStep(otaProgress.mapProgress(0.10f, 0.70f))

    data object ReconnectingAfterTransfer : StmOtaStep(0.70f)

    data object EnteringMainMode : StmOtaStep(0.78f)

    data object ValidatingNewFirmwareVersion : StmOtaStep(0.80f)

    data object ReconnectingAfterValidating : StmOtaStep(0.85f, OtaRecoveryStrategy.SOFT_RESET)

    data object UpdatingUnifiedVersionInformation : StmOtaStep(0.95f, OtaRecoveryStrategy.SOFT_RESET)
}

/**
 * This class represents the different steps that occur during a Un20 (fingerprint sensor) over the
 * air firmware update.
 *
 * NOTE: A bug in UN20 app version 1.0 requires a delay after the transfer step otherwise the UN20 can be
 * bricked, hence the [AwaitingCacheCommit] step.
 */
sealed class Un20OtaStep(
    totalProgress: Float,
    recoveryStrategy: OtaRecoveryStrategy = OtaRecoveryStrategy.SOFT_RESET,
) : OtaStep(totalProgress, recoveryStrategy) {
    data object EnteringMainMode : Un20OtaStep(0.00f)

    data object TurningOnUn20BeforeTransfer : Un20OtaStep(0.05f)

    data object CommencingTransfer : Un20OtaStep(0.15f)

    data class TransferInProgress(
        val otaProgress: Float,
    ) : Un20OtaStep(otaProgress.mapProgress(0.15f, 0.30f))

    data object AwaitingCacheCommit : Un20OtaStep(0.45f, OtaRecoveryStrategy.SOFT_RESET_AFTER_DELAY)

    data object TurningOffUn20AfterTransfer : Un20OtaStep(0.65f)

    data object TurningOnUn20AfterTransfer : Un20OtaStep(0.70f)

    data object ValidatingNewFirmwareVersion : Un20OtaStep(0.80f)

    data object ReconnectingAfterValidating : Un20OtaStep(0.85f)

    data object UpdatingUnifiedVersionInformation : Un20OtaStep(0.95f)
}

/**
 * map a progress value from the interval [0.0, 1.0] to [min, max] linearly
 */
fun Float.mapProgress(
    min: Float,
    max: Float,
): Float = min + (max - min) * this
