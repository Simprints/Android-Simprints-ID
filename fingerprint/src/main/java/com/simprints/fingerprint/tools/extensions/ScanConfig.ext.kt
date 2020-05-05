package com.simprints.fingerprint.tools.extensions

import com.simprints.fingerprint.activities.collect.old.models.FingerScanConfig
import com.simprints.fingerprint.activities.collect.old.models.FingerConfig
import com.simprints.fingerprint.data.domain.fingerprint.FingerIdentifier

fun FingerScanConfig.isFingerNotCollectable(id: FingerIdentifier): Boolean = get(id) == FingerConfig.DO_NOT_COLLECT
