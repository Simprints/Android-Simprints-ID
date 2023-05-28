package com.simprints.infra.rocwrapper

import com.simprints.infra.facebiosdk.detection.FaceDetector
import com.simprints.infra.facebiosdk.initialization.FaceBioSdkInitializer
import com.simprints.infra.facebiosdk.matching.FaceMatcher
import com.simprints.infra.rocwrapper.detection.RankOneFaceDetector
import com.simprints.infra.rocwrapper.initialization.RankOneInitializer
import com.simprints.infra.rocwrapper.matching.RankOneFaceMatcher
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RocWrapperModule {

    @Binds
    abstract fun provideSdkInitializer(impl: RankOneInitializer): FaceBioSdkInitializer

    @Binds
    abstract fun provideFaceDetector(impl: RankOneFaceDetector): FaceDetector

    @Binds
    abstract fun provideFaceMatcher(impl: RankOneFaceMatcher): FaceMatcher
}
