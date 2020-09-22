package com.simprints.id.data.consent.shortconsent

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonProperty

@Keep
data class GeneralConsentOptions(
    @JsonProperty("consent_enrol_only") var consentEnrolOnly: Boolean = false,
    @JsonProperty("consent_enrol") var consentEnrol: Boolean = true,
    @JsonProperty("consent_id_verify") var consentIdVerify: Boolean = true,
    @JsonProperty("consent_share_data_no") var consentShareDataNo: Boolean = true,
    @JsonProperty("consent_share_data_yes") var consentShareDataYes: Boolean = false,
    @JsonProperty("consent_collect_yes") var consentCollectYes: Boolean = false,
    @JsonProperty("consent_privacy_rights") var consentPrivacyRights: Boolean = true,
    @JsonProperty("consent_confirmation") var consentConfirmation: Boolean = true
)
