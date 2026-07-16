package com.simprints.face.infra.basebiosdk.detection

data class SpoofCheckResult(
    val score: Float,
    val skipReason: SkipReason? = null,
) {
    enum class SkipReason {
        NOT_AVAILABLE,
        IMAGE_TOO_SMALL,
        IOD_TOO_SMALL,
        IOD_TOO_LARGE,
    }
}
