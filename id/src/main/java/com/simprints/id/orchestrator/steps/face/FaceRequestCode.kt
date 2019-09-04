package com.simprints.id.orchestrator.steps.face

private const val FACE_REQUEST_CODE = 200

enum class FaceRequestCode(val value: Int) {

    ENROL(FACE_REQUEST_CODE + 1),
    IDENTIFY(FACE_REQUEST_CODE + 2),
    VERIFY(FACE_REQUEST_CODE + 3);

    companion object {
        fun isFaceResult(requestCode: Int) =
            FaceRequestCode.values().map { it.value }.contains(requestCode)
    }
}
