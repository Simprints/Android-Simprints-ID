package com.simprints.document.infra.mlkit

import com.simprints.document.infra.basedocumentsdk.detection.DocumentDetector
import com.simprints.document.infra.basedocumentsdk.initialization.DocumentSdkInitializer
import com.simprints.document.infra.mlkit.detection.MlKitDetector
import com.simprints.document.infra.mlkit.initialization.MlKitInitializer
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class MlKitWrapperModule {
    @Binds
    abstract fun provideSdkInitializer(impl: MlKitInitializer): DocumentSdkInitializer

    @Binds
    abstract fun provideFaceDetector(impl: MlKitDetector): DocumentDetector
}
