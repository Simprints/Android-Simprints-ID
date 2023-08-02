package com.simprints.fingerprint.infra.basebiosdk.detection

import com.simprints.fingerprint.infra.basebiosdk.FingerprintTemplate

fun interface FingerprintTemplateAcquisition {
    // Acquire fingerprint templete  using a scanner and return the template as a FingeprintTemplate object
    fun acquire(scanner:Scanner): FingerprintTemplate

}
