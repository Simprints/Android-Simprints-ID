package com.simprints.id.domain.consent

import android.content.Context
import com.google.gson.annotations.SerializedName
import com.simprints.id.R
import com.simprints.id.session.callout.CalloutAction
import com.simprints.id.session.callout.CalloutAction.IDENTIFY
import com.simprints.id.session.callout.CalloutAction.VERIFY


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

    fun assembleText(context: Context, calloutAction: CalloutAction, programName: String, organisationName: String) = StringBuilder().apply {
        when (calloutAction) {
            IDENTIFY, VERIFY -> {
                if (consentIdVerify) append(context.getString(R.string.consent_id_verify).format(programName))
            }
            else -> {
                if (consentEnrolOnly) append(context.getString(R.string.consent_enrol_only).format(programName))
                if (consentEnrol) append(context.getString(R.string.consent_enrol).format(programName))
            }
        }
        if (consentShareDataNo) append(context.getString(R.string.consent_share_data_no))
        if (consentShareDataYes) append(context.getString(R.string.consent_share_data_yes).format(organisationName))
        if (consentCollectYes) append(context.getString(R.string.consent_collect_yes))
        if (consentPrivacyRights) append(context.getString(R.string.consent_privacy_rights))
        if (consentConfirmation) append(context.getString(R.string.consent_confirmation))
    }.toString()
}
