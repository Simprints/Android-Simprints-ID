package com.simprints.id.activities.longConsent

sealed class PrivacyNoticeState {

    data class ConsentAvailable(val language: String,
                                val consent: String) : PrivacyNoticeState()

    data class ConsentNotAvailable(val language: String) : PrivacyNoticeState()

    data class ConsentNotAvailableBecauseBackendMaintenance(val language: String, val estimatedOutage: Long? = null) :
        PrivacyNoticeState()

    data class DownloadInProgress(val language: String) : PrivacyNoticeState()
}
