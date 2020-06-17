package com.simprints.id.orchestrator.steps.core

private const val CORE_REQUEST_CODE = 300

enum class CoreRequestCode(val value: Int) {
    CONSENT(CORE_REQUEST_CODE + 1),
    FETCH_GUID_CHECK(CORE_REQUEST_CODE + 2),
    EXIT_FORM(CORE_REQUEST_CODE + 3),
    GUID_SELECTION_CODE(CORE_REQUEST_CODE + 4),
    SETUP(CORE_REQUEST_CODE + 5),
    LAST_BIOMETRICS_CORE(CORE_REQUEST_CODE + 6);

    companion object {
        fun isCoreResult(requestCode: Int) =
            CoreRequestCode.values().map { it.value }.contains(requestCode)
    }
}
