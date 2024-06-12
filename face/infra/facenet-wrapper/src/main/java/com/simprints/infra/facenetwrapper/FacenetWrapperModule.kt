package com.simprints.infra.facenetwrapper

import com.simprints.infra.facebiosdk.detection.FaceDetector
import com.simprints.infra.facebiosdk.initialization.FaceBioSdkInitializer
import com.simprints.infra.facebiosdk.matching.FaceMatcher
import com.simprints.infra.facenetwrapper.detection.FaceNetDetector
import com.simprints.infra.facenetwrapper.initialization.FaceNetInitializer
import com.simprints.infra.facenetwrapper.matching.FaceNetMatcher
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class FacenetWrapperModule {

    @Binds
    abstract fun provideSdkInitializer(impl: FaceNetInitializer): FaceBioSdkInitializer

    @Binds
    abstract fun provideFaceDetector(impl: FaceNetDetector): FaceDetector

    @Binds
    abstract fun provideFaceMatcher(impl: FaceNetMatcher): FaceMatcher
}
