package com.simprints.id.activities.alert

import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.crashreport.CrashReportTag
import com.simprints.id.data.analytics.crashreport.CrashReportTrigger
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.analytics.eventdata.models.domain.events.AlertScreenEvent
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.di.AppComponent
import com.simprints.id.domain.alert.AlertActivityViewModel
import com.simprints.id.domain.alert.AlertActivityViewModel.ButtonAction
import com.simprints.id.domain.alert.AlertType
import com.simprints.id.domain.alert.fromAlertToAlertTypeEvent
import com.simprints.id.domain.modality.Modality
import com.simprints.id.tools.TimeHelper
import javax.inject.Inject

class AlertPresenter(val view: AlertContract.View,
                     val component: AppComponent,
                     private val alertType:  AlertType) : AlertContract.Presenter {

    @Inject lateinit var crashReportManager: CrashReportManager
    @Inject lateinit var sessionManager: SessionEventsManager
    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var timeHelper: TimeHelper

    private val alertViewModel = AlertActivityViewModel.fromAlertToAlertViewModel(alertType)

    init {
        component.inject(this)
    }

    override fun start() {
        logToCrashReport()

        initButtons()
        initColours()
        initTextAndDrawables()

        sessionManager.addEventInBackground(AlertScreenEvent(timeHelper.now(), alertType.fromAlertToAlertTypeEvent()))
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
        view.setAlertMessageWithStringRes(alertViewModel.message)
    }

    override fun handleButtonClick(buttonAction: ButtonAction) {
        when (buttonAction) {
            is ButtonAction.None -> Unit
            is ButtonAction.Close -> view.closeActivityAfterCloseButton()
            is ButtonAction.TryAgain -> view.finishWithTryAgain()
            is ButtonAction.WifiSettings -> view.openWifiSettings()
        }
    }

    override fun handleBackButton() {
        if (alertType == AlertType.UNEXPECTED_ERROR || alertType == AlertType.GUID_NOT_FOUND_ONLINE) {
            view.closeActivityAfterCloseButton()
        } else {
            startExitFormActivityBasedOnModalities()
        }
    }

    private fun startExitFormActivityBasedOnModalities() {
        if (isSingleModality()) {
            startModalitySpecificExitForm()
        } else {
            startCoreExitFormActivity()
        }
    }

    private fun isSingleModality() = preferencesManager.modalities.size == 1

    private fun startModalitySpecificExitForm() {
        when (preferencesManager.modalities.first()) {
            Modality.FINGER -> startFingerprintExitFormActivity()
            Modality.FACE -> startFaceExitFormActivity()
        }
    }

    private fun startCoreExitFormActivity() = view.startCoreExitFormActivity()

    private fun startFingerprintExitFormActivity() = view.startFingerprintExitFormActivity()

    private fun startFaceExitFormActivity() = view.startFaceExitFormActivity()

    private fun logToCrashReport() {
        crashReportManager.logMessageForCrashReport(CrashReportTag.ALERT, CrashReportTrigger.UI, message = alertViewModel.name)
    }
}
