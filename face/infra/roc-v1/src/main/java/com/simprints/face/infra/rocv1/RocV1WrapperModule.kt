package com.simprints.face.infra.rocv1

import com.simprints.face.infra.basebiosdk.detection.FaceDetector
import com.simprints.face.infra.basebiosdk.initialization.FaceBioSdkInitializer
import com.simprints.face.infra.basebiosdk.matching.FaceMatcher
import com.simprints.face.infra.rocv1.detection.RocV1Detector
import com.simprints.face.infra.rocv1.initialization.RocV1Initializer
import com.simprints.face.infra.rocv1.matching.RocV1Matcher
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RocV1WrapperModule {
    @Binds
    abstract fun provideSdkInitializer(impl: RocV1Initializer): FaceBioSdkInitializer

    @Binds
    abstract fun provideFaceDetector(impl: RocV1Detector): FaceDetector

    @Binds
    abstract fun provideFaceMatcher(impl: RocV1Matcher): FaceMatcher
}
