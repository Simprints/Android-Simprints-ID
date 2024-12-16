package com.simprints.core

import androidx.camera.camera2.Camera2Config
import androidx.camera.core.CameraXConfig
import androidx.multidex.MultiDexApplication

@ExcludedFromGeneratedTestCoverageReports("Abstract base class")
open class CoreApplication :
    MultiDexApplication(),
    CameraXConfig.Provider {
    override fun getCameraXConfig(): CameraXConfig = Camera2Config.defaultConfig()
}
