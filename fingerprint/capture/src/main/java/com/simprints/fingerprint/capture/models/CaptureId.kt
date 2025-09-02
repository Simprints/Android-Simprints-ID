package com.simprints.fingerprint.capture.models

import com.simprints.core.domain.sample.SampleIdentifier

internal data class CaptureId(
    val finger: SampleIdentifier,
    val captureIndex: Int,
)
