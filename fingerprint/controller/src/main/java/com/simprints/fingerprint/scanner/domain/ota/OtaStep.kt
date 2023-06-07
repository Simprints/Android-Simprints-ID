package com.simprints.fingerprint.scanner.domain.ota

import com.simprints.fingerprint.scanner.domain.ota.Un20OtaStep.AwaitingCacheCommit
import com.simprints.fingerprint.scanner.tools.mapProgress

/**
 * This class represents the ongoing step in an Over the Air update.
 *
 * @property totalProgress  the current progress percentage based on the current step in relation to
 *                          the total number of steps required for a particular firmware update.
 *
 * @property recoveryStrategy  the chosen OTA recovery strategy for a particular step
 *
 */
sealed class OtaStep(val totalProgress: Float, val recoveryStrategy: OtaRecoveryStrategy)

/**
 * This class represents the different steps that occur during a Cypress (bluetooth) firmware over
 * the air update.
 */
sealed class CypressOtaStep(
    totalProgress: Float,
    recoveryStrategy: OtaRecoveryStrategy = OtaRecoveryStrategy.SOFT_RESET
): OtaStep(totalProgress, recoveryStrategy) {

    object EnteringOtaMode: CypressOtaStep(0.00f)
    object CommencingTransfer: CypressOtaStep(0.05f)
    data class TransferInProgress(val otaProgress: Float): CypressOtaStep(otaProgress.mapProgress(0.05f, 0.85f))
    object ReconnectingAfterTransfer: CypressOtaStep(0.85f)
    object ValidatingNewFirmwareVersion: CypressOtaStep(0.90f)
    object UpdatingUnifiedVersionInformation: CypressOtaStep(0.95f)
}

/**
 * This class represents the different steps that occur during an STM firmware update
 *
 * NOTE: A quirk of the STM bootloader causes a disconnect upon first entering STM OTA mode, hence we
 * need to reconnect and enter STM OTA mode a second time.
 */
sealed class StmOtaStep(
    totalProgress: Float,
    recoveryStrategy: OtaRecoveryStrategy = OtaRecoveryStrategy.HARD_RESET
): OtaStep(totalProgress, recoveryStrategy) {

    object EnteringOtaModeFirstTime: StmOtaStep(0.00f)
    object ReconnectingAfterEnteringOtaMode: StmOtaStep(0.05f)
    object EnteringOtaModeSecondTime: StmOtaStep(0.08f)
    object CommencingTransfer: StmOtaStep(0.10f)
    data class TransferInProgress(val otaProgress: Float): StmOtaStep(otaProgress.mapProgress(0.10f, 0.70f))
    object ReconnectingAfterTransfer: StmOtaStep(0.70f)
    object EnteringMainMode: StmOtaStep(0.78f)
    object ValidatingNewFirmwareVersion: StmOtaStep(0.80f)
    object ReconnectingAfterValidating: StmOtaStep(0.85f, OtaRecoveryStrategy.SOFT_RESET)
    object UpdatingUnifiedVersionInformation: StmOtaStep(0.95f, OtaRecoveryStrategy.SOFT_RESET)
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
    recoveryStrategy: OtaRecoveryStrategy = OtaRecoveryStrategy.SOFT_RESET
): OtaStep(totalProgress, recoveryStrategy) {

    object EnteringMainMode: Un20OtaStep(0.00f)
    object TurningOnUn20BeforeTransfer: Un20OtaStep(0.05f)
    object CommencingTransfer: Un20OtaStep(0.15f)
    data class TransferInProgress(val otaProgress: Float): Un20OtaStep(otaProgress.mapProgress(0.15f, 0.30f))
    object AwaitingCacheCommit: Un20OtaStep(0.45f, OtaRecoveryStrategy.SOFT_RESET_AFTER_DELAY)
    object TurningOffUn20AfterTransfer: Un20OtaStep(0.65f)
    object TurningOnUn20AfterTransfer: Un20OtaStep(0.70f)
    object ValidatingNewFirmwareVersion: Un20OtaStep(0.80f)
    object ReconnectingAfterValidating: Un20OtaStep(0.85f)
    object UpdatingUnifiedVersionInformation: Un20OtaStep(0.95f)
}
