package com.simprints.core

import android.app.Application
import androidx.camera.camera2.Camera2Config
import androidx.camera.core.CameraXConfig

@ExcludedFromGeneratedTestCoverageReports("Abstract base class")
open class CoreApplication :
    Application(),
    CameraXConfig.Provider {
    override fun getCameraXConfig(): CameraXConfig = Camera2Config.defaultConfig()
}
