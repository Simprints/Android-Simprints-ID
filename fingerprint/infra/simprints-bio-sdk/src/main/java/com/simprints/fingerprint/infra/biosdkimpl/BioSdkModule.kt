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
import com.simprints.fingerprint.infra.biosdkimpl.matching.SimAfisMatcherSettings
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
        sdkInitializer: SdkInitializer<Unit>,
        fingerprintImageProvider: FingerprintImageProvider<Unit, Unit>,
        fingerprintTemplateProvider: FingerprintTemplateProvider<Unit, Unit>,
        fingerprintMatcher: FingerprintMatcher<SimAfisMatcherSettings>
    ): FingerprintBioSdk<Unit, Unit, Unit, Unit, Unit, SimAfisMatcherSettings> {
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
    internal fun provideFingerprintImageProvider(): FingerprintImageProvider<Unit, Unit> =
        FingerprintImageProviderImpl()

    @Provides
    internal fun provideFingerprintTemplateProvider(): FingerprintTemplateProvider<Unit, Unit> =
        FingerPrintTemplateProviderImpl()

    @Provides
    internal fun provideFingerprintMatcher(simAfisMatcher: SimAfisMatcher): FingerprintMatcher<SimAfisMatcherSettings> =
        FingerprintMatcherImpl(simAfisMatcher)

}




