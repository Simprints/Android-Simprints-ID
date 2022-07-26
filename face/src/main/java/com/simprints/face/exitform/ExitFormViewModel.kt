package com.simprints.face.exitform

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.simprints.core.analytics.CrashReportTag
import com.simprints.core.livedata.LiveDataEvent
import com.simprints.core.livedata.send
import com.simprints.face.capture.FaceCaptureViewModel
import com.simprints.face.controllers.core.events.FaceSessionEventsManager
import com.simprints.face.controllers.core.events.model.RefusalAnswer
import com.simprints.face.controllers.core.events.model.RefusalAnswer.*
import com.simprints.face.controllers.core.events.model.RefusalEvent
import com.simprints.face.controllers.core.timehelper.FaceTimeHelper
import com.simprints.infra.logging.Simber

class ExitFormViewModel(
    private val mainVM: FaceCaptureViewModel,
    val faceSessionEventsManager: FaceSessionEventsManager,
    private val timeHelper: FaceTimeHelper
) : ViewModel() {
    var reason: RefusalAnswer? = null
    var exitFormData: Pair<RefusalAnswer, String>? = null
    var exitFormStartTime: Long = 0

    val requestReasonEvent: MutableLiveData<LiveDataEvent> = MutableLiveData()
    val requestSelectOptionEvent: MutableLiveData<LiveDataEvent> = MutableLiveData()
    val requestFormSubmitEvent: MutableLiveData<LiveDataEvent> = MutableLiveData()

    init {
        exitFormStartTime = timeHelper.now()
    }

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
            exitFormData = Pair(it, exitFormText)
            mainVM.submitExitForm(it, exitFormText)
        }
        logExitFormEvent()
    }

    private fun logRadioOptionForCrashReport(option: String) {
        logMessageForCrashReport("Radio option $option clicked")
    }

    private fun logMessageForCrashReport(message: String) {
        Simber.tag(CrashReportTag.REFUSAL.name).i(message)
    }

    fun logExitFormEvent() {
        if (exitFormData != null) {
            faceSessionEventsManager.addEventInBackground(
                RefusalEvent(
                    startTime = exitFormStartTime,
                    endTime = timeHelper.now(),
                    reason = exitFormData!!.first,
                    otherText = exitFormData!!.second
                )
            )
        }
    }

    fun handleBackButton() {
        if (reason == null) {
            requestSelectOptionEvent.send()
        } else {
            requestFormSubmitEvent.send()
        }
    }
}
