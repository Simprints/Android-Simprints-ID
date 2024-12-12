package com.simprints.fingerprint.infra.necsdkimpl.acquisition.template

import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.Dpi

data class FingerprintTemplateAcquisitionSettings(
    val processingResolution: Dpi?,
    val timeOutMs: Int,
    val qualityThreshold: Int,
    val allowLowQualityExtraction: Boolean,
)
