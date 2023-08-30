package com.simprints.fingerprint.infra.basebiosdk

import com.simprints.fingerprint.infra.basebiosdk.acquization.FingerprintImageProvider
import com.simprints.fingerprint.infra.basebiosdk.acquization.FingerprintTemplateProvider
import com.simprints.fingerprint.infra.basebiosdk.initialization.SdkInitializer
import com.simprints.fingerprint.infra.basebiosdk.matching.FingerprintMatcher
import com.simprints.fingerprint.infra.basebiosdk.matching.domain.FingerprintIdentity

class FingerprintBioSdk<SdkConfig, ImageRequestSettings, ImageResponseMetadata, TemplateRequestSettings, TemplateResponseMetadata, MatcherSettings>(
    private val sdkInitializer: SdkInitializer<SdkConfig>,
    private val fingerprintImageProvider: FingerprintImageProvider<ImageRequestSettings, ImageResponseMetadata>,
    private val fingerprintTemplateProvider: FingerprintTemplateProvider<TemplateRequestSettings, TemplateResponseMetadata>,
    private val fingerprintMatcher: FingerprintMatcher<MatcherSettings>,
) {
    /**
     * Initialize the SDK with the given parameters
     *
     */
    suspend fun initialize(initializationParams: SdkConfig? = null) =
        sdkInitializer.initialize(initializationParams)

    suspend fun acquireFingerprintImage(settings: ImageRequestSettings? = null) =
        fingerprintImageProvider.acquireFingerprintImage(settings)

    suspend fun acquireFingerprintTemplate(settings: TemplateRequestSettings? = null) =
        fingerprintTemplateProvider.acquireFingerprintTemplate(settings)

    suspend fun match(
        probe: FingerprintIdentity,
        candidates: List<FingerprintIdentity>,
        matchingSettings: MatcherSettings?
    ) = fingerprintMatcher.match(probe, candidates, matchingSettings)

}
