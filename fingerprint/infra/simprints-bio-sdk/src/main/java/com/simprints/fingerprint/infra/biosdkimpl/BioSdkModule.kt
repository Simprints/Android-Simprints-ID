package com.simprints.fingerprint.infra.biosdkimpl

import com.simprints.fingerprint.infra.basebiosdk.FingerprintBioSdk
import com.simprints.fingerprint.infra.basebiosdk.acquisition.FingerprintImageProvider
import com.simprints.fingerprint.infra.basebiosdk.acquisition.FingerprintTemplateProvider
import com.simprints.fingerprint.infra.basebiosdk.initialization.SdkInitializer
import com.simprints.fingerprint.infra.basebiosdk.matching.FingerprintMatcher
import com.simprints.fingerprint.infra.biosdkimpl.matching.SimAfisMatcher
import com.simprints.fingerprint.infra.biosdkimpl.acquisition.image.FingerprintImageProviderImpl
import com.simprints.fingerprint.infra.biosdkimpl.acquisition.template.FingerprintTemplateAcquisitionSettings
import com.simprints.fingerprint.infra.biosdkimpl.acquisition.template.FingerprintTemplateMetadata
import com.simprints.fingerprint.infra.biosdkimpl.acquisition.template.FingerprintTemplateProviderImpl
import com.simprints.fingerprint.infra.biosdkimpl.initialization.SdkInitializerImpl
import com.simprints.fingerprint.infra.biosdkimpl.matching.FingerprintMatcherImpl
import com.simprints.fingerprint.infra.biosdkimpl.matching.SimAfisMatcherSettings
import com.simprints.fingerprint.infra.scanner.capture.FingerprintCaptureWrapperFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton


@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class SimprintsSdk

@Module
@InstallIn(SingletonComponent::class)
object SimprintsBioSdkModule {


    @Provides
    @Singleton
    internal fun provideFingerprintBioSdk(
        sdkInitializer: SdkInitializerImpl,
        fingerprintImageProvider: FingerprintImageProviderImpl,
        fingerprintTemplateProvider: FingerprintTemplateProviderImpl,
        fingerprintMatcher: FingerprintMatcherImpl
    ): FingerprintBioSdk<Unit, Unit, Unit, FingerprintTemplateAcquisitionSettings, FingerprintTemplateMetadata, SimAfisMatcherSettings> {
        return FingerprintBioSdk(
            sdkInitializer,
            fingerprintImageProvider,
            fingerprintTemplateProvider,
            fingerprintMatcher
        )
    }

    @Provides
    internal fun provideSdkInitializer(): SdkInitializer<Unit> = SdkInitializerImpl()

    @Provides
    internal fun provideFingerprintImageProvider(fingerprintCaptureWrapperFactory: FingerprintCaptureWrapperFactory): FingerprintImageProvider<Unit,Unit> =
        FingerprintImageProviderImpl(fingerprintCaptureWrapperFactory)

    @Provides
    internal fun provideFingerprintTemplateProvider(fingerprintCaptureWrapperFactory: FingerprintCaptureWrapperFactory): FingerprintTemplateProvider<FingerprintTemplateAcquisitionSettings, FingerprintTemplateMetadata> =
        FingerprintTemplateProviderImpl(fingerprintCaptureWrapperFactory )

    @Provides
    internal fun provideFingerprintMatcher(simAfisMatcher: SimAfisMatcher): FingerprintMatcher<SimAfisMatcherSettings> =
        FingerprintMatcherImpl(simAfisMatcher)
}




