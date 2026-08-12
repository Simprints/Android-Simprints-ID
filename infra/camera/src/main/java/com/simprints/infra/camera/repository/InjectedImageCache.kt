package com.simprints.infra.camera.repository

import android.graphics.Bitmap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InjectedImageCache @Inject constructor() {
    @Volatile
    var injectedImage: Bitmap? = null
        internal set
}
