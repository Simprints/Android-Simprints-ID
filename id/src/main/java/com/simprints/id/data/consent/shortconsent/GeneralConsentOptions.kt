package com.simprints.id.data.consent.shortconsent

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class GeneralConsentOptions(
    @SerializedName("consent_enrol_only") var consentEnrolOnly: Boolean = false,
    @SerializedName("consent_enrol") var consentEnrol: Boolean = true,
    @SerializedName("consent_id_verify") var consentIdVerify: Boolean = true,
    @SerializedName("consent_share_data_no") var consentShareDataNo: Boolean = true,
    @SerializedName("consent_share_data_yes") var consentShareDataYes: Boolean = false,
    @SerializedName("consent_collect_yes") var consentCollectYes: Boolean = false,
    @SerializedName("consent_privacy_rights") var consentPrivacyRights: Boolean = true,
    @SerializedName("consent_confirmation") var consentConfirmation: Boolean = true
)
