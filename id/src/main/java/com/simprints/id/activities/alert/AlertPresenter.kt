package com.simprints.id.activities.alert

import com.simprints.core.tools.extentions.inBackground
import com.simprints.id.R
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.crashreport.CrashReportTag
import com.simprints.id.data.analytics.crashreport.CrashReportTrigger
import com.simprints.id.data.db.event.EventRepository
import com.simprints.id.data.db.event.domain.events.AlertScreenEvent
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.di.AppComponent
import com.simprints.id.domain.alert.AlertActivityViewModel
import com.simprints.id.domain.alert.AlertActivityViewModel.ButtonAction
import com.simprints.id.domain.alert.AlertActivityViewModel.ENROLMENT_LAST_BIOMETRICS_FAILED
import com.simprints.id.domain.alert.AlertType
import com.simprints.id.domain.alert.fromAlertToAlertTypeEvent
import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.modality.Modality.FACE
import com.simprints.id.domain.modality.Modality.FINGER
import com.simprints.id.exitformhandler.ExitFormHelper
import com.simprints.id.tools.TimeHelper
import javax.inject.Inject

class AlertPresenter(val view: AlertContract.View,
                     val component: AppComponent,
                     private val alertType: AlertType) : AlertContract.Presenter {

    @Inject lateinit var crashReportManager: CrashReportManager
    @Inject lateinit var eventRepository: EventRepository
    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var timeHelper: TimeHelper
    @Inject lateinit var exitFormHelper: ExitFormHelper

    private val alertViewModel = AlertActivityViewModel.fromAlertToAlertViewModel(alertType)

    init {
        component.inject(this)
    }

    override fun start() {
        logToCrashReport()

        initButtons()
        initColours()
        initTextAndDrawables()

        inBackground { eventRepository.addEvent(AlertScreenEvent(timeHelper.now(), alertType.fromAlertToAlertTypeEvent())) }
    }

    private fun initButtons() {
        view.initLeftButton(alertViewModel.leftButton)
        view.initRightButton(alertViewModel.rightButton)
    }

    private fun initColours() {
        val color = view.getColorForColorRes(alertViewModel.backgroundColor)
        view.setLayoutBackgroundColor(color)
        view.setLeftButtonBackgroundColor(color)
        view.setRightButtonBackgroundColor(color)
    }

    private fun initTextAndDrawables() {
        view.setAlertTitleWithStringRes(alertViewModel.title)
        view.setAlertImageWithDrawableId(alertViewModel.mainDrawable)
        view.setAlertHintImageWithDrawableId(alertViewModel.hintDrawable)
        view.setAlertMessageWithStringRes(alertViewModel.message, getParamsForMessageString().toTypedArray())
    }

    private fun getParamsForMessageString(): List<Any> {
        return if (alertViewModel == ENROLMENT_LAST_BIOMETRICS_FAILED) {
            with(preferencesManager.modalities) {
                when {
                    isFingerprintAndFace() -> { listOf(view.getTranslatedString(R.string.enrol_last_biometrics_alert_message_all_param)) }
                    isFace() -> { listOf(view.getTranslatedString(R.string.enrol_last_biometrics_alert_message_face_param)) }
                    isFingerprint() -> { listOf(view.getTranslatedString(R.string.enrol_last_biometrics_alert_message_fingerprint_param)) }
                    else -> {
                        emptyList<String>()
                    }
                }
            }
        } else {
            emptyList<String>()
        }
    }

    private fun List<Modality>.isFingerprintAndFace() = containsAll(listOf(FACE, FINGER))
    private fun List<Modality>.isFingerprint() = contains(FINGER) && this.size == 1
    private fun List<Modality>.isFace() = contains(FACE) && this.size == 1

    override fun handleButtonClick(buttonAction: ButtonAction) {
        when (buttonAction) {
            is ButtonAction.None -> Unit
            is ButtonAction.Close -> view.closeActivityAfterCloseButton()
            is ButtonAction.TryAgain -> view.finishWithTryAgain()
            is ButtonAction.WifiSettings -> view.openWifiSettings()
        }
    }

    override fun handleBackButton() {
        when (alertType) {
            AlertType.UNEXPECTED_ERROR,
            AlertType.GUID_NOT_FOUND_ONLINE,
            AlertType.DIFFERENT_PROJECT_ID_SIGNED_IN,
            AlertType.DIFFERENT_USER_ID_SIGNED_IN,
            AlertType.ENROLMENT_LAST_BIOMETRICS_FAILED,
            AlertType.SAFETYNET_ERROR -> {
                view.closeActivityAfterCloseButton()
            }
            AlertType.GUID_NOT_FOUND_OFFLINE -> {
                startExitFormActivity()
            }
        }
    }

    private fun startExitFormActivity() {
        view.startExitForm(exitFormHelper.getExitFormActivityClassFromModalities(preferencesManager.modalities))
    }

    private fun logToCrashReport() {
        crashReportManager.logMessageForCrashReport(CrashReportTag.ALERT, CrashReportTrigger.UI, message = alertViewModel.name)
    }
}
