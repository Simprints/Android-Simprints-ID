package com.simprints.fingerprint.capture.models

import com.simprints.core.domain.reference.TemplateIdentifier

internal data class CaptureId(
    val finger: TemplateIdentifier,
    val captureIndex: Int,
)
