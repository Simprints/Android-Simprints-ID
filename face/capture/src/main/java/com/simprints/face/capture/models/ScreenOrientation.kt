package com.simprints.face.capture.models

import android.content.res.Configuration
import android.content.res.Resources

enum class ScreenOrientation {
    Landscape, Portrait;

    companion object {
        fun getCurrentOrientation(resources: Resources) =
            when (resources.configuration.orientation) {
                Configuration.ORIENTATION_LANDSCAPE -> Landscape
                else -> Portrait
            }
    }
}