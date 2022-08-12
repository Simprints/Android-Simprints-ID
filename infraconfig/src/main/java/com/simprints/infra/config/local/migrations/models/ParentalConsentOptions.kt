package com.simprints.infra.config.local.migrations.models

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonProperty
import com.simprints.infra.config.domain.models.ConsentConfiguration

@Keep
data class ParentalConsentOptions(
    @JsonProperty("consent_parent_enrol_only") var consentParentEnrolOnly: Boolean = false,
    @JsonProperty("consent_parent_enrol") var consentParentEnrol: Boolean = true,
    @JsonProperty("consent_parent_id_verify") var consentParentIdVerify: Boolean = true,
    @JsonProperty("consent_parent_share_data_no") var consentParentShareDataNo: Boolean = true,
    @JsonProperty("consent_parent_share_data_yes") var consentParentShareDataYes: Boolean = false,
    @JsonProperty("consent_parent_collect_yes") var consentParentCollectYes: Boolean = false,
    @JsonProperty("consent_parent_privacy_rights") var consentParentPrivacyRights: Boolean = true,
    @JsonProperty("consent_parent_confirmation") var consentParentConfirmation: Boolean = true
) {
    fun toDomain(): ConsentConfiguration.ConsentPromptConfiguration =
        ConsentConfiguration.ConsentPromptConfiguration(
            enrolmentVariant = if (consentParentEnrolOnly) {
                ConsentConfiguration.ConsentEnrolmentVariant.STANDARD
            } else {
                ConsentConfiguration.ConsentEnrolmentVariant.ENROLMENT_ONLY
            },
            dataSharedWithPartner = consentParentShareDataYes,
            dataUsedForRAndD = consentParentCollectYes,
            privacyRights = consentParentPrivacyRights,
            confirmation = consentParentConfirmation,
        )
}
