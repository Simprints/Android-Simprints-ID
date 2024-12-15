package com.simprints.fingerprint.infra.simafiswrapper

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SimAfisWrapperModule {
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
