package com.simprints.fingerprint.activities.collect.state

class FingerScanResult(
    val qualityScore: Int,
    val template: ByteArray,
    val image: ByteArray?
)
