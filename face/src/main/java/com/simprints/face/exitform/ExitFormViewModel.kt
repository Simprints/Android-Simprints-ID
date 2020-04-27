package com.simprints.face.exitform

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.simprints.core.livedata.LiveDataEvent
import com.simprints.core.livedata.send
import com.simprints.face.capture.FaceCaptureViewModel
import com.simprints.face.data.domain.exitform.RefusalFormReason
import com.simprints.face.data.domain.exitform.RefusalFormReason.*

class ExitFormViewModel(private val mainVM: FaceCaptureViewModel) : ViewModel() {
    private var reason: RefusalFormReason = OTHER

    val requestReasonEvent: MutableLiveData<LiveDataEvent> = MutableLiveData()

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
        reason = SCANNER_NOT_WORKING
        requestReasonEvent.send()
        logRadioOptionForCrashReport("App not working")
    }

    fun handleOtherRadioOptionClick() {
        reason = OTHER
        requestReasonEvent.send()
        logRadioOptionForCrashReport("Other")
    }

    fun submitExitForm(exitFormText: String) {
        // TODO: create the correct answer and send back
    }

    private fun logRadioOptionForCrashReport(option: String) {
        logMessageForCrashReport("Radio option $option clicked")
    }

    private fun logMessageForCrashReport(message: String) {
        // TODO: log on the crash report manager
//        crashReportManager.logMessageForCrashReport(CrashReportTag.REFUSAL, CrashReportTrigger.UI, message = message)
    }
}
