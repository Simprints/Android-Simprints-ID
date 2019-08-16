package com.simprints.id.data.consent.shortconsent

import android.content.Context
import com.simprints.id.R
import com.simprints.id.domain.moduleapi.app.requests.AppIdentifyRequest
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.requests.AppVerifyRequest

data class GeneralConsentData(val generalConsentOptions: GeneralConsentOptions,
                              val programName: String,
                              val organizationName: String) {

    fun assembleText(context: Context, appRequest: AppRequest) = StringBuilder().apply {
        when (appRequest) {
            is AppIdentifyRequest, is AppVerifyRequest -> {
                if (generalConsentOptions.consentIdVerify) append(context.getString(R.string.consent_id_verify).format(programName))
            }
            else -> {
                if (generalConsentOptions.consentEnrolOnly) append(context.getString(R.string.consent_enrol_only).format(programName))
                if (generalConsentOptions.consentEnrol) append(context.getString(R.string.consent_enrol).format(programName))
            }
        }
        if (generalConsentOptions.consentShareDataNo) append(context.getString(R.string.consent_share_data_no))
        if (generalConsentOptions.consentShareDataYes) append(context.getString(R.string.consent_share_data_yes).format(organizationName))
        if (generalConsentOptions.consentCollectYes) append(context.getString(R.string.consent_collect_yes))
        if (generalConsentOptions.consentPrivacyRights) append(context.getString(R.string.consent_privacy_rights))
        if (generalConsentOptions.consentConfirmation) append(context.getString(R.string.consent_confirmation))
    }.toString()
}
