package com.simprints.fingerprint.infra.basebiosdk

import com.simprints.fingerprint.infra.basebiosdk.acquization.FingerprintImageProvider
import com.simprints.fingerprint.infra.basebiosdk.acquization.FingerprintTemplateProvider
import com.simprints.fingerprint.infra.basebiosdk.initialization.SdkInitializer
import com.simprints.fingerprint.infra.basebiosdk.matching.FingerprintMatcher
import com.simprints.fingerprint.infra.basebiosdk.matching.domain.FingerprintIdentity

class FingerprintBioSdk(
    private val sdkInitializer: SdkInitializer,
    private val fingerprintImageProvider: FingerprintImageProvider,
    private val fingerprintTemplateProvider: FingerprintTemplateProvider,
    private val fingerprintMatcher: FingerprintMatcher,
) {
    /**
     * Initialize the SDK with the given parameters
     *
     * @param params
     */
    fun initialize(params: Map<String, Any>) = sdkInitializer.initialize(params)
    fun acquireFingerprintImage() = fingerprintImageProvider.acquireFingerprintImage()
    fun acquireFingerprintTemplate() = fingerprintTemplateProvider.acquireFingerprintTemplate()

    fun match(
        probe: FingerprintIdentity,
        candidates: List<FingerprintIdentity>,
        crossFingerComparison: Boolean
    ) = fingerprintMatcher.match(probe, candidates, crossFingerComparison)

}
