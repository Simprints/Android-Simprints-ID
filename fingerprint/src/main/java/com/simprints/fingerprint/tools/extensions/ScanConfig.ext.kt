package com.simprints.fingerprint.tools.extensions

import com.simprints.fingerprint.activities.collect.models.DefaultScanConfig
import com.simprints.fingerprint.activities.collect.models.FingerConfig
import com.simprints.fingerprint.data.domain.fingerprint.FingerIdentifier

fun DefaultScanConfig.isFingerNotCollectable(id: FingerIdentifier): Boolean = get(id) == FingerConfig.DO_NOT_COLLECT
