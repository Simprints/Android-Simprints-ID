package com.simprints.id.activities.consent

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.JsonSyntaxException
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.analytics.eventdata.models.domain.events.ConsentEvent
import com.simprints.id.data.analytics.eventdata.models.domain.events.ConsentEvent.Result.ACCEPTED
import com.simprints.id.data.analytics.eventdata.models.domain.events.ConsentEvent.Result.DECLINED
import com.simprints.id.data.analytics.eventdata.models.domain.events.ConsentEvent.Type.INDIVIDUAL
import com.simprints.id.data.analytics.eventdata.models.domain.events.ConsentEvent.Type.PARENTAL
import com.simprints.id.data.consent.shortconsent.*
import com.simprints.id.tools.TimeHelper

class ConsentViewModel(private val consentDataManager: ConsentDataManager,
                       private val crashReportManager: CrashReportManager,
                       private val sessionEventsManager: SessionEventsManager,
                       private val programName: String,
                       private val organizationName: String,
                       private val timeHelper: TimeHelper) : ViewModel() {

    val generalConsentData = MutableLiveData<GeneralConsentData>()
    val parentalConsentData = MutableLiveData<ParentalConsentData>()
    var isConsentTabGeneral = true
    private var startConsentEventTime: Long = 0
    var consentAcceptClick = MutableLiveData<Boolean>()

    init {
        generalConsentData.postValue(getGeneralConsentData())
        parentalConsentData.postValue(getParentalConsentData())
        startConsentEventTime = timeHelper.now()
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
        addConsentEvent(ACCEPTED)
        consentAcceptClick.postValue(true)
    }

    fun handleConsentDeclineClick() {
        addConsentEvent(DECLINED)
        consentAcceptClick.postValue(false)
    }

    private fun addConsentEvent(result: ConsentEvent.Result) {
        sessionEventsManager.addEventInBackground(
            ConsentEvent(
                startConsentEventTime,
                timeHelper.now(),
                if (isConsentTabGeneral) {
                    INDIVIDUAL
                } else {
                    PARENTAL
                },
                result))
    }
}
