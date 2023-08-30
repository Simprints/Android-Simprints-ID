package com.simprints.fingerprint.infra.biosdkimpl.acquisition.image

import com.simprints.fingerprint.infra.basebiosdk.acquization.FingerprintImageProvider
import com.simprints.fingerprint.infra.basebiosdk.acquization.domain.AcquireFingerprintImageResponse
import com.simprints.fingerprint.infra.scanner.capture.FingerprintCaptureWrapperFactory
import javax.inject.Inject

internal class FingerprintImageProviderImpl @Inject constructor(private val fingerprintCaptureWrapperFactory: FingerprintCaptureWrapperFactory) :
    FingerprintImageProvider<Unit, Unit> {

    override suspend fun acquireFingerprintImage(settings: Unit?): AcquireFingerprintImageResponse<Unit> =
        AcquireFingerprintImageResponse(fingerprintCaptureWrapperFactory.captureWrapper.acquireFingerprintImage().imageBytes)
}
