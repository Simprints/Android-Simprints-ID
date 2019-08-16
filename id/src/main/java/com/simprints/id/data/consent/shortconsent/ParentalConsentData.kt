package com.simprints.id.data.consent.shortconsent

import android.content.Context
import com.simprints.id.R
import com.simprints.id.domain.moduleapi.app.requests.AppIdentifyRequest
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.requests.AppVerifyRequest

data class ParentalConsentData(val parentalConsentExists: Boolean,
                               val parentalConsentOptions: ParentalConsentOptions,
                               val programName: String,
                               val organizationName: String) {

    fun assembleText(context: Context, appRequest: AppRequest) = StringBuilder().apply {
        when (appRequest) {
            is AppIdentifyRequest, is AppVerifyRequest -> {
                if (parentalConsentOptions.consentParentIdVerify) append(context.getString(R.string.consent_parental_id_verify).format(programName))
            }
            else -> {
                if (parentalConsentOptions.consentParentEnrolOnly) append(context.getString(R.string.consent_parental_enrol_only).format(programName))
                if (parentalConsentOptions.consentParentEnrol) append(context.getString(R.string.consent_parental_enrol).format(programName))
            }
        }
        if (parentalConsentOptions.consentParentShareDataNo) append(context.getString(R.string.consent_parental_share_data_no))
        if (parentalConsentOptions.consentParentShareDataYes) append(context.getString(R.string.consent_parental_share_data_yes).format(organizationName))
        if (parentalConsentOptions.consentCollectYes) append(context.getString(R.string.consent_collect_yes))
        if (parentalConsentOptions.consentParentPrivacyRights) append(context.getString(R.string.consent_parental_privacy_rights))
        if (parentalConsentOptions.consentParentConfirmation) append(context.getString(R.string.consent_parental_confirmation))
    }.toString()
}
