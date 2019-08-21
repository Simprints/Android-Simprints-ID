package com.simprints.id.data.consent.shortconsent

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ParentalConsentOptions(
    @SerializedName("consent_parent_enrol_only") var consentParentEnrolOnly: Boolean = false,
    @SerializedName("consent_parent_enrol") var consentParentEnrol: Boolean = true,
    @SerializedName("consent_parent_id_verify") var consentParentIdVerify: Boolean = true,
    @SerializedName("consent_parent_share_data_no") var consentParentShareDataNo: Boolean = true,
    @SerializedName("consent_parent_share_data_yes") var consentParentShareDataYes: Boolean = false,
    @SerializedName("consent_collect_yes") var consentCollectYes: Boolean = false,
    @SerializedName("consent_parent_privacy_rights") var consentParentPrivacyRights: Boolean = true,
    @SerializedName("consent_parent_confirmation") var consentParentConfirmation: Boolean = true
)
