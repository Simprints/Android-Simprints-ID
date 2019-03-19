package com.simprints.fingerprint.activities.alert

import android.app.Activity.RESULT_CANCELED
import com.simprints.fingerprint.data.domain.alert.FingerprintAlert
import com.simprints.fingerprint.data.domain.alert.request.AlertActRequest
import com.simprints.fingerprint.di.FingerprintsComponent
import com.simprints.fingerprint.tools.utils.TimeHelper
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.crashreport.CrashReportTag
import com.simprints.id.data.analytics.crashreport.CrashReportTrigger
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.analytics.eventdata.models.domain.events.AlertScreenEvent
import javax.inject.Inject

class AlertPresenter(val view: AlertContract.View,
                     val component: FingerprintsComponent,
                     alertActRequest: AlertActRequest?) : AlertContract.Presenter {

    private val alertType = alertActRequest?.alert ?: FingerprintAlert.UNEXPECTED_ERROR

    @Inject lateinit var crashReportManager: CrashReportManager
    @Inject lateinit var sessionManager: SessionEventsManager
    @Inject lateinit var timeHelper: TimeHelper

    init {
        component.inject(this)
    }

    override fun start() {
        logToCrashReport()

        initButtons()
        initColours()
        initTextAndDrawables()

        sessionManager.updateSessionInBackground {
            it.addEvent(AlertScreenEvent(it.timeRelativeToStartTime(timeHelper.now()), alertType.name))
        }
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
        crashReportManager.logMessageForCrashReport(CrashReportTag.ALERT, CrashReportTrigger.UI, message = alertType.name)
    }
}
