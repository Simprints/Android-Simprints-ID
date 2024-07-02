package com.simprints.face.infra.rocv1

import com.simprints.face.infra.basebiosdk.detection.FaceDetector
import com.simprints.face.infra.basebiosdk.initialization.FaceBioSdkInitializer
import com.simprints.face.infra.basebiosdk.matching.FaceMatcher
import com.simprints.face.infra.rocv1.detection.RankOneFaceDetector
import com.simprints.face.infra.rocv1.initialization.RankOneInitializer
import com.simprints.face.infra.rocv1.matching.RankOneFaceMatcher
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RocV1WrapperModule {

    @Binds
    abstract fun provideSdkInitializer(impl: RankOneInitializer): FaceBioSdkInitializer

    @Binds
    abstract fun provideFaceDetector(impl: RankOneFaceDetector): FaceDetector

    @Binds
    abstract fun provideFaceMatcher(impl: RankOneFaceMatcher): FaceMatcher
}
