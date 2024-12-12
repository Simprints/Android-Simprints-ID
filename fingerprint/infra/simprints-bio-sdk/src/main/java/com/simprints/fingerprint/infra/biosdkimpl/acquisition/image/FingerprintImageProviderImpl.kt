package com.simprints.fingerprint.infra.biosdkimpl.acquisition.image

import com.simprints.fingerprint.infra.basebiosdk.acquisition.FingerprintImageProvider
import com.simprints.fingerprint.infra.basebiosdk.acquisition.domain.ImageResponse
import com.simprints.fingerprint.infra.scanner.capture.FingerprintCaptureWrapperFactory
import javax.inject.Inject

internal class FingerprintImageProviderImpl @Inject constructor(
    private val fingerprintCaptureWrapperFactory: FingerprintCaptureWrapperFactory,
) : FingerprintImageProvider<Unit, Unit> {
    override suspend fun acquireFingerprintImage(settings: Unit?): ImageResponse<Unit> =
        ImageResponse(fingerprintCaptureWrapperFactory.captureWrapper.acquireFingerprintImage().imageBytes)
}
