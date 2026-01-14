package com.simprints.infra.config.store.local.migrations.models

import androidx.annotation.Keep
import com.simprints.infra.config.store.models.ConsentConfiguration
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class ParentalConsentOptions(
    @SerialName("consent_parent_enrol_only") var consentParentEnrolOnly: Boolean = false,
    @SerialName("consent_parent_enrol") var consentParentEnrol: Boolean = true,
    @SerialName("consent_parent_id_verify") var consentParentIdVerify: Boolean = true,
    @SerialName("consent_parent_share_data_no") var consentParentShareDataNo: Boolean = true,
    @SerialName("consent_parent_share_data_yes") var consentParentShareDataYes: Boolean = false,
    @SerialName("consent_parent_collect_yes") var consentParentCollectYes: Boolean = false,
    @SerialName("consent_parent_privacy_rights") var consentParentPrivacyRights: Boolean = true,
    @SerialName("consent_parent_confirmation") var consentParentConfirmation: Boolean = true,
) {
    fun toDomain(): ConsentConfiguration.ConsentPromptConfiguration = ConsentConfiguration.ConsentPromptConfiguration(
        enrolmentVariant = if (consentParentEnrolOnly) {
            ConsentConfiguration.ConsentEnrolmentVariant.ENROLMENT_ONLY
        } else {
            ConsentConfiguration.ConsentEnrolmentVariant.STANDARD
        },
        dataSharedWithPartner = consentParentShareDataYes,
        dataUsedForRAndD = consentParentCollectYes,
        privacyRights = consentParentPrivacyRights,
        confirmation = consentParentConfirmation,
    )
}
