package com.simprints.fingerprint.infra.necsdkimpl

import com.simprints.fingerprint.infra.basebiosdk.FingerprintBioSdk
import com.simprints.fingerprint.infra.necsdkimpl.acquisition.image.FingerprintImageProviderImpl
import com.simprints.fingerprint.infra.necsdkimpl.acquisition.template.FingerprintTemplateAcquisitionSettings
import com.simprints.fingerprint.infra.necsdkimpl.acquisition.template.FingerprintTemplateMetadata
import com.simprints.fingerprint.infra.necsdkimpl.acquisition.template.FingerprintTemplateProviderImpl
import com.simprints.fingerprint.infra.necsdkimpl.initialization.SdkInitializerImpl
import com.simprints.fingerprint.infra.necsdkimpl.matching.FingerprintMatcherImpl
import com.simprints.fingerprint.infra.necsdkimpl.matching.NecMatchingSettings
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class NecSdk

@Module
@InstallIn(SingletonComponent::class)
object NecSdkModule {

    @Provides
    @Singleton
    internal fun provideFingerprintBioSdk(
        sdkInitializer: SdkInitializerImpl,
        fingerprintImageProvider: FingerprintImageProviderImpl,
        fingerprintTemplateProvider: FingerprintTemplateProviderImpl,
        fingerprintMatcher: FingerprintMatcherImpl
    ): FingerprintBioSdk<Unit, Unit, Unit, FingerprintTemplateAcquisitionSettings, FingerprintTemplateMetadata, NecMatchingSettings> {
        return FingerprintBioSdk(
            sdkInitializer,
            fingerprintImageProvider,
            fingerprintTemplateProvider,
            fingerprintMatcher
        )
    }


}
