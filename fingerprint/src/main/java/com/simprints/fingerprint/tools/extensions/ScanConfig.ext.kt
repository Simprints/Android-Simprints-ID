package com.simprints.fingerprint.tools.extensions

import com.simprints.fingerprint.activities.collect.models.FingerConfig
import com.simprints.fingerprint.activities.collect.models.FingerIdentifier
import com.simprints.fingerprint.activities.collect.models.ScanConfig

fun ScanConfig.isFingerNotCollectable(id: FingerIdentifier): Boolean = get(id) == FingerConfig.DO_NOT_COLLECT
