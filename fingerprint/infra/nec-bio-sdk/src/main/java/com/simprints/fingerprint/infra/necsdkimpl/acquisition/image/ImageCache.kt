package com.simprints.fingerprint.infra.necsdkimpl.acquisition.image

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageCache @Inject constructor() {
    var lastCaptureImage: ByteArray? = null

}
