package com.simprints.infra.config.domain

data class ProjectConfiguration(
    val projectId: String,
    val general: GeneralConfiguration,
    val face: FaceConfiguration?,
    val fingerprint: FingerprintConfiguration?,
    val consent: ConsentConfiguration,
    val identification: IdentificationConfiguration,
    val synchronization: SynchronizationConfiguration,
)
