package com.simprints.id.activities.consent

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.JsonSyntaxException
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.consent.shortconsent.*

class ConsentViewModel(private val consentDataManager: ConsentDataManager,
                       private val crashReportManager: CrashReportManager,
                       private val programName: String,
                       private val organizationName: String) : ViewModel() {

    val generalConsentData = MutableLiveData<GeneralConsentData>()
    val parentalConsentData = MutableLiveData<ParentalConsentData>()

    fun start() {
        generalConsentData.postValue(getGeneralConsentData())
        parentalConsentData.postValue(getParentalConsentData())
    }

    private fun getGeneralConsentData() =
        GeneralConsentData(getGeneralConsentOptions(),
            programName, organizationName)

    private fun getGeneralConsentOptions() = try {
        JsonHelper.gson.fromJson(consentDataManager.generalConsentOptionsJson, GeneralConsentOptions::class.java)
    } catch (e: JsonSyntaxException) {
        crashReportManager.logExceptionOrSafeException(Exception("Malformed General Consent Text Error", e))
        GeneralConsentOptions()
    }

    private fun getParentalConsentData() =
        ParentalConsentData(consentDataManager.parentalConsentExists,
            getParentalConsentOptions(),
            programName, organizationName)

    private fun getParentalConsentOptions() = try {
        JsonHelper.gson.fromJson(consentDataManager.parentalConsentOptionsJson, ParentalConsentOptions::class.java)
    } catch (e: JsonSyntaxException) {
        crashReportManager.logExceptionOrSafeException(Exception("Malformed Parental Consent Text Error", e))
        ParentalConsentOptions()
    }

    fun handleConsentAcceptClick() {

    }

    fun handleConsentDeclineClick() {

    }
}
