package com.simprints.id.tools.extensions

import com.simprints.id.FingerIdentifier
import com.simprints.id.domain.fingerprint.FingerConfig
import com.simprints.id.domain.fingerprint.ScanConfig

fun ScanConfig.isFingerNotCollectable(id: FingerIdentifier): Boolean = get(id) == FingerConfig.DO_NOT_COLLECT
