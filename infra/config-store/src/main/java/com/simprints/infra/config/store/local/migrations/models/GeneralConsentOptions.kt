package com.simprints.infra.config.store.local.migrations.models

import androidx.annotation.Keep
import com.simprints.infra.config.store.models.ConsentConfiguration
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class GeneralConsentOptions(
    @SerialName("consent_enrol_only") var consentEnrolOnly: Boolean = false,
    @SerialName("consent_enrol") var consentEnrol: Boolean = true,
    @SerialName("consent_id_verify") var consentIdVerify: Boolean = true,
    @SerialName("consent_share_data_no") var consentShareDataNo: Boolean = true,
    @SerialName("consent_share_data_yes") var consentShareDataYes: Boolean = false,
    @SerialName("consent_collect_yes") var consentCollectYes: Boolean = false,
    @SerialName("consent_privacy_rights") var consentPrivacyRights: Boolean = true,
    @SerialName("consent_confirmation") var consentConfirmation: Boolean = true,
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
