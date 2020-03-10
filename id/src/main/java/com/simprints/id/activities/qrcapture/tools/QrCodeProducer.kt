package com.simprints.id.activities.qrcapture.tools

import androidx.camera.core.UseCase
import kotlinx.coroutines.channels.Channel

interface QrCodeProducer {

    val qrCodeChannel: Channel<String>
    val imageAnalyser: UseCase
}
