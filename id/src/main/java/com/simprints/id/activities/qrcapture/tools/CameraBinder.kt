package com.simprints.id.activities.qrcapture.tools

import androidx.camera.core.Preview
import androidx.camera.core.UseCase
import androidx.lifecycle.LifecycleOwner

interface CameraBinder {
    fun bindToLifecycle(lifecycleOwner: LifecycleOwner, preview: Preview, useCase: UseCase)
}
