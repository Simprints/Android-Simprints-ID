package com.simprints.fingerprint.activities.alert

import android.app.Activity.RESULT_CANCELED
import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportManager
import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportTag.ALERT
import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportTrigger.UI
import com.simprints.fingerprint.controllers.core.eventData.FingerprintSessionEventsManager
import com.simprints.fingerprint.controllers.core.timehelper.FingerprintTimeHelper
import com.simprints.fingerprint.data.domain.alert.FingerprintAlert
import com.simprints.fingerprint.data.domain.alert.request.AlertActRequest
import com.simprints.fingerprint.di.FingerprintComponent
import javax.inject.Inject

class AlertPresenter(val view: AlertContract.View,
                     val component: FingerprintComponent,
                     alertActRequest: AlertActRequest?) : AlertContract.Presenter {

    private val alertType = alertActRequest?.alert ?: FingerprintAlert.UNEXPECTED_ERROR

    @Inject lateinit var crashReportManager: FingerprintCrashReportManager
    @Inject lateinit var sessionManager: FingerprintSessionEventsManager
    @Inject lateinit var timeHelper: FingerprintTimeHelper

    init {
        component.inject(this)
    }

    override fun start() {
        logToCrashReport()

        initButtons()
        initColours()
        initTextAndDrawables()

        sessionManager.addAlertEventInBackground(timeHelper.now(), alertType)
    }

    private fun initButtons() {
        view.initLeftButton(alertType.leftButton)
        view.initRightButton(alertType.rightButton)
    }

    private fun initColours() {
        val color = view.getColorForColorRes(alertType.backgroundColor)
        view.setLayoutBackgroundColor(color)
        view.setLeftButtonBackgroundColor(color)
        view.setRightButtonBackgroundColor(color)
    }

    private fun initTextAndDrawables() {
        view.setAlertTitleWithStringRes(alertType.title)
        view.setAlertImageWithDrawableId(alertType.mainDrawable)
        view.setAlertHintImageWithDrawableId(alertType.hintDrawable)
        view.setAlertMessageWithStringRes(alertType.message)
    }

    override fun handleButtonClick(buttonAction: FingerprintAlert.ButtonAction) {
        buttonAction.resultCode?.let { view.setResult(it) }
        when (buttonAction) {
            is FingerprintAlert.ButtonAction.None -> Unit
            is FingerprintAlert.ButtonAction.WifiSettings -> view.openWifiSettings()
            is FingerprintAlert.ButtonAction.BluetoothSettings -> view.openBluetoothSettings()
            is FingerprintAlert.ButtonAction.TryAgain -> view.closeActivity()
            is FingerprintAlert.ButtonAction.Close -> view.closeAllActivities()
        }
    }

    override fun handleBackButton() {
        view.setResult(RESULT_CANCELED)
    }

    private fun logToCrashReport() {
        crashReportManager.logMessageForCrashReport(ALERT, UI, message = alertType.name)
    }
}
