package com.simprints.id.orchestrator.steps.face

private const val FACE_REQUEST_CODE = 110

enum class FaceRequestCode(val value: Int) {

    CAPTURE(FACE_REQUEST_CODE + 1),
    MATCH(FACE_REQUEST_CODE + 2),
    CONFIGURATION(FACE_REQUEST_CODE + 3);

    companion object {
        fun isFaceResult(requestCode: Int) = values().map { it.value }.contains(requestCode)
    }
}