package com.simprints.fingerprint.infra.biosdkimpl.acquisition.template

import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.Dpi

data class FingerprintTemplateAcquisitionSettings(
    val captureFingerprintDpi: Dpi?,
    val timeOutMs: Int,
    val qualityThreshold: Int,
    val allowLowQualityExtraction: Boolean = false,
)
