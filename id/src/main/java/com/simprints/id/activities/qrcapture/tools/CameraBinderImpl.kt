package com.simprints.id.activities.qrcapture.tools

import androidx.camera.core.CameraX
import androidx.camera.core.Preview
import androidx.camera.core.UseCase
import androidx.lifecycle.LifecycleOwner

class CameraBinderImpl : CameraBinder {

    override fun bindToLifecycle(
        lifecycleOwner: LifecycleOwner,
        preview: Preview,
        useCase: UseCase
    ) {
        CameraX.bindToLifecycle(lifecycleOwner, preview, useCase)
    }

}
