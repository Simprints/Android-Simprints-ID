package com.simprints.infra.config.store.models

import com.simprints.core.domain.common.AgeGroup

/**
 * Common parts of SDK configurations that are used across modules.
 */
interface ModalitySdkConfiguration {
    val decisionPolicy: DecisionPolicy
    val verificationMatchThreshold: Float?
    val allowedAgeRange: AgeGroup
}
