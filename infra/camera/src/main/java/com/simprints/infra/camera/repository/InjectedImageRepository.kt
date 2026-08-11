package com.simprints.infra.camera.repository

import android.graphics.Bitmap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InjectedImageRepository @Inject constructor() {
    @Volatile
    var injectedImage: Bitmap? = null
        internal set
}
