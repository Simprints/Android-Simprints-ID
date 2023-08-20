package com.simprints.fingerprint.infra.basebiosdk

import com.simprints.fingerprint.infra.basebiosdk.acquization.FingerprintImageProvider
import com.simprints.fingerprint.infra.basebiosdk.acquization.FingerprintTemplateProvider
import com.simprints.fingerprint.infra.basebiosdk.initialization.SdkInitializer
import com.simprints.fingerprint.infra.basebiosdk.matching.FingerprintMatcher
import com.simprints.fingerprint.infra.basebiosdk.matching.SimAfisMatcher
import com.simprints.fingerprint.infra.biosdkimpl.acquization.FingerPrintTemplateProviderImpl
import com.simprints.fingerprint.infra.biosdkimpl.acquization.FingerprintImageProviderImpl
import com.simprints.fingerprint.infra.biosdkimpl.initialization.SdkInitializerImpl
import com.simprints.fingerprint.infra.biosdkimpl.matching.FingerprintMatcherImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object BioSdkModule {

    @Provides
    @Singleton
    fun provideFingerprintBioSdk(
        sdkInitializer: SdkInitializer,
        fingerprintImageProvider: FingerprintImageProvider,
        fingerprintTemplateProvider: FingerprintTemplateProvider,
        fingerprintMatcher: FingerprintMatcher
    ): FingerprintBioSdk {
        return FingerprintBioSdk(
            sdkInitializer,
            fingerprintImageProvider,
            fingerprintTemplateProvider,
            fingerprintMatcher
        )
    }

    @Provides
    internal fun provideSdkInitializer(): SdkInitializer = SdkInitializerImpl()

    @Provides
    internal fun provideFingerprintImageProvider(): FingerprintImageProvider =
        FingerprintImageProviderImpl()

    @Provides
    internal fun provideFingerprintTemplateProvider(): FingerprintTemplateProvider =
        FingerPrintTemplateProviderImpl()

    @Provides
    internal fun provideFingerprintMatcher(simAfisMatcher: SimAfisMatcher): FingerprintMatcher =
        FingerprintMatcherImpl(simAfisMatcher)

}




