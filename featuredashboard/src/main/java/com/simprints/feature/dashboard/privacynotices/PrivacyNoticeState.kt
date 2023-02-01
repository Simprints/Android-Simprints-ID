package com.simprints.feature.dashboard.privacynotices

internal sealed class PrivacyNoticeState {

    data class Available(
        val language: String,
        val privacyNotice: String
    ) : PrivacyNoticeState()

    data class NotAvailable(val language: String) : PrivacyNoticeState()

    data class NotAvailableBecauseBackendMaintenance(
        val language: String,
        val estimatedOutage: Long? = null
    ) : PrivacyNoticeState()

    data class DownloadInProgress(val language: String) : PrivacyNoticeState()

    data class NotConnectedToInternet(val language: String) : PrivacyNoticeState()
}
