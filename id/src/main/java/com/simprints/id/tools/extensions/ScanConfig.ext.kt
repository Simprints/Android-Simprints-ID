package com.simprints.id.tools.extensions

import com.simprints.id.FingerIdentifier
import com.simprints.id.activities.collectFingerprints.models.FingerConfig
import com.simprints.id.activities.collectFingerprints.models.ScanConfig

fun ScanConfig.isFingerNotCollectable(id: FingerIdentifier): Boolean = get(id) == FingerConfig.DO_NOT_COLLECT
