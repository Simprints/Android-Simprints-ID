package com.simprints.id.data.consent.shortconsent

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.JsonSyntaxException
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.domain.moduleapi.app.requests.AppRequest

class ConsentTextManagerImpl(private val context: Context,
                             private val consentDataManager: ConsentDataManager,
                             private val crashReportManager: CrashReportManager,
                             private val programName: String,
                             private val organizationName: String) : ConsentTextManager {

    private val generalConsentText = MutableLiveData<String>()
    private val parentalConsentText = MutableLiveData<String>()
    private val parentalConsentExists = MutableLiveData(false)

    override fun getGeneralConsentText(appRequest: AppRequest): LiveData<String> {
        generalConsentText.postValue(getGeneralConsentData().assembleText(context, appRequest))
        return generalConsentText
    }

    override fun parentalConsentExists(): LiveData<Boolean> {
        parentalConsentExists.postValue(consentDataManager.parentalConsentExists)
        return parentalConsentExists
    }

    override fun getParentalConsentText(appRequest: AppRequest): LiveData<String> {
        parentalConsentText.postValue(getParentalConsentData().assembleText(context, appRequest))
        return parentalConsentText
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
}
