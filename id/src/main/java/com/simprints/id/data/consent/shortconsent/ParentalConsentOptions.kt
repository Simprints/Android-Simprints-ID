package com.simprints.id.data.consent.shortconsent

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonProperty

@Keep
data class ParentalConsentOptions(
    @JsonProperty("consent_parent_enrol_only") var consentParentEnrolOnly: Boolean = false,
    @JsonProperty("consent_parent_enrol") var consentParentEnrol: Boolean = true,
    @JsonProperty("consent_parent_id_verify") var consentParentIdVerify: Boolean = true,
    @JsonProperty("consent_parent_share_data_no") var consentParentShareDataNo: Boolean = true,
    @JsonProperty("consent_parent_share_data_yes") var consentParentShareDataYes: Boolean = false,
    @JsonProperty("consent_parent_collect_yes") var consentParentalCollectYes: Boolean = false,
    @JsonProperty("consent_parent_privacy_rights") var consentParentPrivacyRights: Boolean = true,
    @JsonProperty("consent_parent_confirmation") var consentParentConfirmation: Boolean = true
)
