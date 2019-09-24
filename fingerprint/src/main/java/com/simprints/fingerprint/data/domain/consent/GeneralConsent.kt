package com.simprints.fingerprint.data.domain.consent

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.simprints.fingerprint.R
import com.simprints.fingerprint.controllers.core.androidResources.FingerprintAndroidResourcesHelper
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintIdentifyRequest
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintRequest
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintVerifyRequest

@Keep
data class GeneralConsent(
    @SerializedName("consent_enrol_only") var consentEnrolOnly: Boolean = false,
    @SerializedName("consent_enrol") var consentEnrol: Boolean = true,
    @SerializedName("consent_id_verify") var consentIdVerify: Boolean = true,
    @SerializedName("consent_share_data_no") var consentShareDataNo: Boolean = true,
    @SerializedName("consent_share_data_yes") var consentShareDataYes: Boolean = false,
    @SerializedName("consent_collect_yes") var consentCollectYes: Boolean = false,
    @SerializedName("consent_privacy_rights") var consentPrivacyRights: Boolean = true,
    @SerializedName("consent_confirmation") var consentConfirmation: Boolean = true
) {

    fun assembleText(androidResourcesHelper: FingerprintAndroidResourcesHelper,
                     fingerprint: FingerprintRequest,
                     programName: String,
                     organisationName: String) = StringBuilder().apply {
        when (fingerprint) {
            is FingerprintIdentifyRequest, is FingerprintVerifyRequest -> {
                if (consentIdVerify) append(androidResourcesHelper.getString(R.string.consent_id_verify).format(programName))
            }
            else -> {
                if (consentEnrolOnly) append(androidResourcesHelper.getString(R.string.consent_enrol_only).format(programName))
                if (consentEnrol) append(androidResourcesHelper.getString(R.string.consent_enrol).format(programName))
            }
        }
        if (consentShareDataNo) append(androidResourcesHelper.getString(R.string.consent_share_data_no))
        if (consentShareDataYes) append(androidResourcesHelper.getString(R.string.consent_share_data_yes).format(organisationName))
        if (consentCollectYes) append(androidResourcesHelper.getString(R.string.consent_collect_yes))
        if (consentPrivacyRights) append(androidResourcesHelper.getString(R.string.consent_privacy_rights))
        if (consentConfirmation) append(androidResourcesHelper.getString(R.string.consent_confirmation))
    }.toString()
}
