package com.simprints.id.tools.extensions

import com.simprints.libcommon.FingerConfig
import com.simprints.libcommon.ScanConfig
import com.simprints.libsimprints.FingerIdentifier

fun ScanConfig.isFingerRequired(id: FingerIdentifier): Boolean = get(id) == FingerConfig.REQUIRED
fun ScanConfig.isFingerOptional(id: FingerIdentifier): Boolean = get(id) == FingerConfig.OPTIONAL
fun ScanConfig.isFingerNotCollectable(id: FingerIdentifier): Boolean = get(id) == FingerConfig.DO_NOT_COLLECT
