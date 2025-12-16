package com.simprints.fingerprint.infra.basebiosdk

import com.simprints.core.domain.reference.BiometricReferenceCapture
import com.simprints.core.domain.reference.CandidateRecord
import com.simprints.fingerprint.infra.basebiosdk.acquisition.FingerprintImageProvider
import com.simprints.fingerprint.infra.basebiosdk.acquisition.FingerprintTemplateProvider
import com.simprints.fingerprint.infra.basebiosdk.initialization.SdkInitializer
import com.simprints.fingerprint.infra.basebiosdk.matching.FingerprintMatcher

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
    suspend fun initialize(initializationParams: SdkConfig? = null) = sdkInitializer.initialize(initializationParams)

    suspend fun acquireFingerprintImage(settings: ImageRequestSettings? = null) = fingerprintImageProvider.acquireFingerprintImage(settings)

    suspend fun acquireFingerprintTemplate(settings: TemplateRequestSettings? = null) =
        fingerprintTemplateProvider.acquireFingerprintTemplate(settings)

    suspend fun match(
        probeReference: BiometricReferenceCapture,
        candidates: List<CandidateRecord>,
        matchingSettings: MatcherSettings?,
    ) = fingerprintMatcher.match(probeReference, candidates, matchingSettings)

    val supportedTemplateFormat: String
        get() = fingerprintMatcher.supportedTemplateFormat
    val matcherName: String
        get() = fingerprintMatcher.matcherName
}
