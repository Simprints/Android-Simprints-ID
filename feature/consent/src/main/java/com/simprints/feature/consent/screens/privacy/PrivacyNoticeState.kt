package com.simprints.feature.consent.screens.privacy

internal sealed class PrivacyNoticeState {
    data class ConsentAvailable(
        val consent: String,
    ) : PrivacyNoticeState()

    object ConsentNotAvailable : PrivacyNoticeState()

    data class BackendMaintenance(
        val estimatedOutage: String? = null,
    ) : PrivacyNoticeState()

    object DownloadInProgress : PrivacyNoticeState()
}
