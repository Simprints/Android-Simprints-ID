package com.simprints.fingerprintmatcher

import com.simprints.fingerprintmatcher.algorithms.simafis.JNILibAfis
import com.simprints.fingerprintmatcher.algorithms.simafis.JNILibAfisInterface
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(ActivityComponent::class)
abstract class FingerprintMatcherModule {

    @Binds
    internal abstract fun provideFingerprintMatcher(impl: FingerprintMatcherImpl): FingerprintMatcher

    @Binds
    internal abstract fun provideJNILibAfis(impl: JNILibAfis): JNILibAfisInterface
}

@Module
@InstallIn(SingletonComponent::class)
object JNILibAfisModule {

    @Provides
    @Singleton
    fun provideJNILibAfis(): JNILibAfis = JNILibAfis
}
