package com.simprints.id.activities.consent

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.JsonSyntaxException
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.consent.shortconsent.ConsentDataManager
import com.simprints.id.data.consent.shortconsent.GeneralConsent
import com.simprints.id.data.consent.shortconsent.ParentalConsent
import com.simprints.id.domain.moduleapi.fingerprint.requests.FingerprintEnrolRequest

class ConsentViewModel(private val consentDataManager: ConsentDataManager,
                       private val crashReportManager: CrashReportManager,
                       private val fingerprintRequest: FingerprintEnrolRequest,
                       private val context: Context) : ViewModel() {

    var generalConsentText = MutableLiveData<String>()
    var parentalConsentText = MutableLiveData<String>()

    fun start() {
        generalConsentText.postValue(getGeneralConsentText())
        parentalConsentText.postValue(getParentalConsentText())
    }

    private fun getGeneralConsentText(): String {
        val generalConsent = try {
            JsonHelper.gson.fromJson(consentDataManager.generalConsentOptionsJson, GeneralConsent::class.java)
        } catch (e: JsonSyntaxException) {
            crashReportManager.logExceptionOrSafeException(Exception("Malformed General Consent Text Error", e))
            GeneralConsent()
        }
        return generalConsent.assembleText(context, fingerprintRequest, fingerprintRequest.programName, fingerprintRequest.organizationName)
    }

    private fun getParentalConsentText(): String {
        val parentalConsent = try {
            JsonHelper.gson.fromJson(consentDataManager.parentalConsentOptionsJson, ParentalConsent::class.java)
        } catch (e: JsonSyntaxException) {
            crashReportManager.logExceptionOrSafeException(Exception("Malformed Parental Consent Text Error", e))
            ParentalConsent()
        }
        return parentalConsent.assembleText(context, fingerprintRequest, fingerprintRequest.programName, fingerprintRequest.organizationName)
    }

    fun handleConsentAcceptClick() {

    }

    fun handleConsentDeclineClick() {

    }
}
