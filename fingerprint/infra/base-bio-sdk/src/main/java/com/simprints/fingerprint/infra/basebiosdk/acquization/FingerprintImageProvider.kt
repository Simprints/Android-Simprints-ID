package com.simprints.fingerprint.infra.basebiosdk.acquization

import com.simprints.fingerprint.infra.basebiosdk.acquization.domain.AcquireFingerprintImageResponse
import com.simprints.fingerprint.infra.basebiosdk.acquization.domain.SaveFingerprintImagesStrategy

fun interface FingerprintImageProvider {
    fun captureFingerprintImage(saveFingerprintImagesStrategy: SaveFingerprintImagesStrategy?): AcquireFingerprintImageResponse

}
