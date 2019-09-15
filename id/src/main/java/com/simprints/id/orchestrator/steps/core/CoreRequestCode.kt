package com.simprints.id.orchestrator.steps.core

private const val CORE_REQUEST_CODE = 300

enum class CoreRequestCode(val value: Int) {
    CONSENT(CORE_REQUEST_CODE + 1),
    VERIFICATION_CHECK(CORE_REQUEST_CODE + 2);

    companion object {
        fun isCoreResult(requestCode: Int) =
            CoreRequestCode.values().map { it.value }.contains(requestCode)
    }
}
