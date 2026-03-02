package com.simprints.face.infra.simface

import com.simprints.biometrics.simpalm.SimPalm
import com.simprints.face.infra.basebiosdk.detection.FaceDetector
import com.simprints.face.infra.basebiosdk.initialization.FaceBioSdkInitializer
import com.simprints.face.infra.simface.detection.SimFaceDetector
import com.simprints.face.infra.simface.initialization.SimFaceInitializer
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SimFaceProviderModule {
    @Provides
    @Singleton
    fun provideSimPalm(): SimPalm = SimPalm()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class SimFaceModule {
    @Binds
    abstract fun provideSimFaceSdkInitializer(impl: SimFaceInitializer): FaceBioSdkInitializer

    @Binds
    abstract fun provideSimFaceDetector(impl: SimFaceDetector): FaceDetector
}
