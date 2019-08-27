package com.simprints.id.orchestrator.steps.fingerprint

private const val FINGERPRINT_REQUEST_CODE = 100

enum class FingerprintRequestCode(val value: Int) {

    ENROL(FINGERPRINT_REQUEST_CODE + 1),
    IDENTIFY(FINGERPRINT_REQUEST_CODE + 2),
    VERIFY(FINGERPRINT_REQUEST_CODE + 3);

    companion object {
        fun isFingerprintResult(requestCode: Int) =
            FingerprintRequestCode.values().map { it.value }.contains(requestCode)
    }
}

