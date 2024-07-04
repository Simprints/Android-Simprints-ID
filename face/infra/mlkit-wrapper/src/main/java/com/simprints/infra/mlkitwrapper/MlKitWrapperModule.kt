package com.simprints.infra.mlkitwrapper

import com.simprints.infra.facebiosdk.detection.FaceDetector
import com.simprints.infra.facebiosdk.initialization.FaceBioSdkInitializer
import com.simprints.infra.facebiosdk.matching.FaceMatcher
import com.simprints.infra.mlkitwrapper.detection.MlKitDetector
import com.simprints.infra.mlkitwrapper.initialization.MlKitInitializer
import com.simprints.infra.mlkitwrapper.matching.MlKitMatcher
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class MlKitWrapperModule {

    @Binds
    abstract fun provideSdkInitializer(impl: MlKitInitializer): FaceBioSdkInitializer

    @Binds
    abstract fun provideFaceDetector(impl: MlKitDetector): FaceDetector

    @Binds
    abstract fun provideFaceMatcher(impl: MlKitMatcher): FaceMatcher
}
