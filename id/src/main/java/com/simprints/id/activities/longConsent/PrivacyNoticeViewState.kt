package com.simprints.id.activities.longConsent

sealed class PrivacyNoticeViewState {

    data class ConsentAvailable(val language: String,
                                val consent: String) : PrivacyNoticeViewState()

    data class ConsentNotAvailable(val language: String) : PrivacyNoticeViewState()

    data class DownloadInProgress(val language: String,
                                  val progress: Int) : PrivacyNoticeViewState()
}
