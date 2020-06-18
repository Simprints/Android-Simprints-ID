package com.simprints.face.exitform

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.simprints.core.livedata.LiveDataEvent
import com.simprints.core.livedata.send
import com.simprints.face.capture.FaceCaptureViewModel
import com.simprints.face.controllers.core.crashreport.FaceCrashReportManager
import com.simprints.face.controllers.core.crashreport.FaceCrashReportTag.REFUSAL
import com.simprints.face.controllers.core.crashreport.FaceCrashReportTrigger.UI
import com.simprints.face.controllers.core.events.model.RefusalAnswer
import com.simprints.face.controllers.core.events.model.RefusalAnswer.*

class ExitFormViewModel(
    private val mainVM: FaceCaptureViewModel,
    private val crashReportManager: FaceCrashReportManager
) : ViewModel() {
    private var reason: RefusalAnswer? = null

    val requestReasonEvent: MutableLiveData<LiveDataEvent> = MutableLiveData()
    val requestSelectOptionEvent: MutableLiveData<LiveDataEvent> = MutableLiveData()
    val requestFormSubmitEvent: MutableLiveData<LiveDataEvent> = MutableLiveData()

    fun handleReligiousConcernsRadioClick() {
        reason = REFUSED_RELIGION
        logRadioOptionForCrashReport("Religious concerns")
    }

    fun handleDataConcernsRadioClick() {
        reason = REFUSED_DATA_CONCERNS
        logRadioOptionForCrashReport("Data concerns")
    }

    fun handleTooYoungRadioClick() {
        reason = REFUSED_YOUNG
        logRadioOptionForCrashReport("Too young")
    }

    fun handlePersonNotPresentRadioClick() {
        reason = REFUSED_NOT_PRESENT
        logRadioOptionForCrashReport("Person not present")
    }

    fun handleDoesNotHavePermissionRadioClick() {
        reason = REFUSED_PERMISSION
        logRadioOptionForCrashReport("Does not have permission")
    }

    fun handleAppNotWorkingRadioClick() {
        reason = APP_NOT_WORKING
        requestReasonEvent.send()
        logRadioOptionForCrashReport("App not working")
    }

    fun handleOtherRadioOptionClick() {
        reason = OTHER
        requestReasonEvent.send()
        logRadioOptionForCrashReport("Other")
    }

    fun submitExitForm(exitFormText: String) {
        reason?.let {
            logExitFormEvent()
            mainVM.submitExitForm(it, exitFormText)
        }
    }

    private fun logRadioOptionForCrashReport(option: String) {
        logMessageForCrashReport("Radio option $option clicked")
    }

    private fun logMessageForCrashReport(message: String) {
        crashReportManager.logMessageForCrashReport(
            REFUSAL,
            UI,
            message = message
        )
    }

    private fun logExitFormEvent() {
        // TODO: log correct refusal event
    }

    fun handleBackButton() {
        if (reason == null) {
            requestSelectOptionEvent.send()
        } else {
            requestFormSubmitEvent.send()
        }
    }
}
