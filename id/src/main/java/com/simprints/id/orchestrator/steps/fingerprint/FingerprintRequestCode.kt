package com.simprints.id.orchestrator.steps.fingerprint

private const val FINGERPRINT_REQUEST_CODE = 100

enum class FingerprintRequestCode(val value: Int) {

    CAPTURE(FINGERPRINT_REQUEST_CODE + 1),
    MATCH(FINGERPRINT_REQUEST_CODE + 2);

    companion object {
        fun isFingerprintResult(requestCode: Int) = values().any { it.value == requestCode }
    }
}

