package com.simprints.infra.config.store.models

sealed class PrivacyNoticeResult(
    open val language: String,
) {
    data class Succeed(
        override val language: String,
        val consent: String,
    ) : PrivacyNoticeResult(language)

    data class Failed(
        override val language: String,
        val error: Throwable,
    ) : PrivacyNoticeResult(language)

    data class FailedBecauseBackendMaintenance(
        override val language: String,
        val error: Throwable,
        val estimatedOutage: Long? = null,
    ) : PrivacyNoticeResult(language)

    data class InProgress(
        override val language: String,
    ) : PrivacyNoticeResult(language)
}
