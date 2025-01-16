package com.simprints.feature.orchestrator.usecases

import com.simprints.core.DeviceID
import javax.inject.Inject

internal class FaceAutoCaptureEligibilityUseCase @Inject constructor(
    @DeviceID private val deviceID: String,
) {

    /*
        Face auto-capture configuration example - enabled for device1 and device2:
        ```json
        "faceAutoCapture": {
            "enabled": true,
            "deviceIdFilter": ["device1", "device2"]
        }
        ```
        If no deviceIdFilter: entire device pool eligible.
        If no config at all: no auto-capture.
     */
    operator fun invoke(
        faceAutoCaptureConfig: Map<String, *>?,
    ): Boolean {
        if (faceAutoCaptureConfig == null) {
            return false
        }
        if (faceAutoCaptureConfig[FACE_AUTO_CAPTURE_ENABLED] != true) {
            return false
        }
        if (faceAutoCaptureConfig[FACE_AUTO_CAPTURE_DEVICE_ID_FILTER] == null) {
            return true
        }
        val faceAutoCaptureDeviceIdsFiltered: List<String> =
            (faceAutoCaptureConfig[FACE_AUTO_CAPTURE_DEVICE_ID_FILTER] as? List<*>)
                ?.mapNotNull { it as? String }
                ?: emptyList()
        return deviceID in faceAutoCaptureDeviceIdsFiltered
    }

    private companion object {
        private const val FACE_AUTO_CAPTURE_ENABLED = "enabled"
        private const val FACE_AUTO_CAPTURE_DEVICE_ID_FILTER = "deviceIdFilter"
    }
}
