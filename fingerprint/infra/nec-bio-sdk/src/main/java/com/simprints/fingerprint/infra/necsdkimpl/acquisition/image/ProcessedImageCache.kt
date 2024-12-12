package com.simprints.fingerprint.infra.necsdkimpl.acquisition.image

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Processed image cache
 * This class stores the recently captured image
 *
 */
@Singleton
internal class ProcessedImageCache @Inject constructor() {
    var recentlyCapturedImage: ByteArray? = null
}
