package com.simprints.id.tools

class InternalConstants {

    class QrCapture {
        companion object {
            const val QR_SCAN_RESULT_KEY = "SCAN_RESULT"
            const val QR_SCAN_ERROR_KEY = "QR_SCAN_ERROR_KEY"
        }

        enum class QrCaptureError {
            GENERAL_ERROR,
            PERMISSION_NOT_GRANTED,
            CAMERA_NOT_AVAILABLE
        }
    }
}
