package com.simprints.fingerprint.infra.basebiosdk

import com.simprints.fingerprint.infra.basebiosdk.matching.FingerprintMatcher
import com.simprints.fingerprint.infra.biosdkimpl.matching.FingerprintMatcherImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
abstract class BioSdkModule {


    //Todo:  the module will provide initializer and detector for fingerprint
    @Binds
    abstract fun provideFingerprintMatcher(impl: FingerprintMatcherImpl): FingerprintMatcher


}




