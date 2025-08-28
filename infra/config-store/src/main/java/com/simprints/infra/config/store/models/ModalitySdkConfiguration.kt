package com.simprints.infra.config.store.models

import com.simprints.core.domain.modality.Modality

interface ModalitySdkType

interface ModalitySdkConfiguration {
    val decisionPolicy: DecisionPolicy
    val verificationMatchThreshold: Float?
    val allowedAgeRange: AgeGroup
}

typealias ModalitySdkConfigurationMapping = Map<Modality, Map<ModalitySdkType, ModalitySdkConfiguration>>

fun ProjectConfiguration.getModalityConfigs(): ModalitySdkConfigurationMapping {
    val fingerprintConfigs = buildMap<ModalitySdkType, ModalitySdkConfiguration> {
        fingerprint?.secugenSimMatcher?.let { put(FingerprintConfiguration.BioSdk.SECUGEN_SIM_MATCHER, it) }
        fingerprint?.nec?.let { put(FingerprintConfiguration.BioSdk.NEC, it) }
    }

    val faceConfigs = buildMap<ModalitySdkType, ModalitySdkConfiguration> {
        face?.rankOne?.let { put(FaceConfiguration.BioSdk.RANK_ONE, it) }
        face?.simFace?.let { put(FaceConfiguration.BioSdk.SIM_FACE, it) }
    }

    return buildMap {
        if (fingerprintConfigs.isNotEmpty()) {
            put(Modality.FINGERPRINT, fingerprintConfigs)
        }
        if (faceConfigs.isNotEmpty()) {
            put(Modality.FACE, faceConfigs)
        }
    }
}
