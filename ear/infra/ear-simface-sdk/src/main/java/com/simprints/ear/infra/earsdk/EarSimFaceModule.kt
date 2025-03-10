package com.simprints.ear.infra.earsdk

import android.content.Context
import com.simprints.ear.infra.basebiosdk.detection.EarDetector
import com.simprints.ear.infra.basebiosdk.initialization.EarBioSdkInitializer
import com.simprints.ear.infra.basebiosdk.matching.EarMatcher
import com.simprints.simface.core.SimFaceConfig
import com.simprints.simface.core.SimFaceFacade
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object EarSimFaceProviderModule {

    @Provides
    fun provideSimFaceFacade(
        @ApplicationContext context: Context,
    ): SimFaceFacade {
        SimFaceFacade.initialize(SimFaceConfig(context))
        return SimFaceFacade.getInstance()
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class EarSimFaceModule {
    @Binds
    abstract fun provideEarSdkInitializer(impl: EarSimFaceInitializer): EarBioSdkInitializer

    @Binds
    abstract fun provideEarDetector(impl: EarSimFaceDetector): EarDetector

    @Binds
    abstract fun provideEarMatcher(impl: EarSimFaceMatcher): EarMatcher
}
