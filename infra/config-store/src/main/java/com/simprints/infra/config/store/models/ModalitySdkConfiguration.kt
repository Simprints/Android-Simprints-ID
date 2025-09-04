package com.simprints.infra.config.store.models

/**
 * Common parts of SDK configurations that are used across modules.
 */
interface ModalitySdkConfiguration {
    val decisionPolicy: DecisionPolicy
    val verificationMatchThreshold: Float?
    val allowedAgeRange: AgeGroup
}
