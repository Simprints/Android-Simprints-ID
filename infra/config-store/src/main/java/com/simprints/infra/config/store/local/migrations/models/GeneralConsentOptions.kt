package com.simprints.infra.config.store.local.migrations.models

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonProperty
import com.simprints.infra.config.store.models.ConsentConfiguration

@Keep
data class GeneralConsentOptions(
    @JsonProperty("consent_enrol_only") var consentEnrolOnly: Boolean = false,
    @JsonProperty("consent_enrol") var consentEnrol: Boolean = true,
    @JsonProperty("consent_id_verify") var consentIdVerify: Boolean = true,
    @JsonProperty("consent_share_data_no") var consentShareDataNo: Boolean = true,
    @JsonProperty("consent_share_data_yes") var consentShareDataYes: Boolean = false,
    @JsonProperty("consent_collect_yes") var consentCollectYes: Boolean = false,
    @JsonProperty("consent_privacy_rights") var consentPrivacyRights: Boolean = true,
    @JsonProperty("consent_confirmation") var consentConfirmation: Boolean = true,
) {
    fun toDomain(): ConsentConfiguration.ConsentPromptConfiguration = ConsentConfiguration.ConsentPromptConfiguration(
        enrolmentVariant = if (consentEnrol) {
            ConsentConfiguration.ConsentEnrolmentVariant.STANDARD
        } else {
            ConsentConfiguration.ConsentEnrolmentVariant.ENROLMENT_ONLY
        },
        dataSharedWithPartner = consentShareDataYes,
        dataUsedForRAndD = consentCollectYes,
        privacyRights = consentPrivacyRights,
        confirmation = consentConfirmation,
    )
}
