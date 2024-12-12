package com.simprints.face.infra.rocv3

import com.simprints.face.infra.basebiosdk.detection.FaceDetector
import com.simprints.face.infra.basebiosdk.initialization.FaceBioSdkInitializer
import com.simprints.face.infra.basebiosdk.matching.FaceMatcher
import com.simprints.face.infra.rocv3.detection.RocV3Detector
import com.simprints.face.infra.rocv3.initialization.RocV3Initializer
import com.simprints.face.infra.rocv3.matching.RocV3Matcher
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RocV3WrapperModule {
    @Binds
    abstract fun provideSdkInitializer(impl: RocV3Initializer): FaceBioSdkInitializer

    @Binds
    abstract fun provideFaceDetector(impl: RocV3Detector): FaceDetector

    @Binds
    abstract fun provideFaceMatcher(impl: RocV3Matcher): FaceMatcher
}
