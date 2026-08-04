package com.simprints.id.camera

import com.simprints.infra.camera.CameraFrameProvider
import com.simprints.infra.camera.StandardCameraFrameProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
abstract class CameraFrameProviderModule {
    @Binds
    abstract fun bindCameraFrameProviderFactory(impl: StandardCameraFrameProvider.Factory): CameraFrameProvider.Factory
}
