package com.simprints.id.data.consent.shortconsent

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.google.gson.JsonSyntaxException
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.domain.moduleapi.core.requests.AskConsentRequest

class ConsentRepositoryImpl(private val context: Context,
                            private val consentLocalDataSource: ConsentLocalDataSource,
                            private val crashReportManager: CrashReportManager,
                            private val programName: String,
                            private val organizationName: String,
                            language: String) : ConsentRepository {

    private val generalConsentText = MutableLiveData<String>()
    private val parentalConsentText = MutableLiveData<String>()
    private val parentalConsentExists = MutableLiveData(false)

    override fun getGeneralConsentText(askConsentRequest: AskConsentRequest) = generalConsentText.apply {
        postValue(getGeneralConsentData().assembleText(context, askConsentRequest))
    }

    override fun parentalConsentExists() = parentalConsentExists.apply {
        postValue(consentLocalDataSource.parentalConsentExists)
    }

    override fun getParentalConsentText(askConsentRequest: AskConsentRequest) = parentalConsentText.apply {
        postValue(getParentalConsentData().assembleText(context, askConsentRequest))
    }

    private fun getGeneralConsentData() =
        GeneralConsentDataGenerator(getGeneralConsentOptions(),
            programName, organizationName)

    private fun getGeneralConsentOptions() = try {
        JsonHelper.gson.fromJson(consentLocalDataSource.generalConsentOptionsJson, GeneralConsentOptions::class.java)
    } catch (e: JsonSyntaxException) {
        crashReportManager.logExceptionOrSafeException(Exception("Malformed General Consent Text Error", e))
        GeneralConsentOptions()
    }

    private fun getParentalConsentData() =
        ParentalConsentDataGenerator(consentLocalDataSource.parentalConsentExists,
            getParentalConsentOptions(),
            programName, organizationName)

    private fun getParentalConsentOptions() = try {
        JsonHelper.gson.fromJson(consentLocalDataSource.parentalConsentOptionsJson, ParentalConsentOptions::class.java)
    } catch (e: JsonSyntaxException) {
        crashReportManager.logExceptionOrSafeException(Exception("Malformed Parental Consent Text Error", e))
        ParentalConsentOptions()
    }
}
