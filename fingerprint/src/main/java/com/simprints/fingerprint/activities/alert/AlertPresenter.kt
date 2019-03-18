package com.simprints.fingerprint.activities.alert

import android.app.Activity.RESULT_CANCELED
import com.simprints.fingerprint.data.domain.alert.FingerprintAlert
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
                     val alert: FingerprintAlert) : AlertContract.Presenter {

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
            it.addEvent(AlertScreenEvent(it.timeRelativeToStartTime(timeHelper.now()), alert.name))
        }
    }

    private fun initButtons() {
        view.initLeftButton(alert.leftButton)
        view.initRightButton(alert.rightButton)
    }

    private fun initColours() {
        val color = view.getColorForColorRes(alert.backgroundColor)
        view.setLayoutBackgroundColor(color)
        view.setLeftButtonBackgroundColor(color)
        view.setRightButtonBackgroundColor(color)
    }

    private fun initTextAndDrawables() {
        view.setAlertTitleWithStringRes(alert.title)
        view.setAlertImageWithDrawableId(alert.mainDrawable)
        view.setAlertHintImageWithDrawableId(alert.hintDrawable)
        view.setAlertMessageWithStringRes(alert.message)
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
        crashReportManager.logMessageForCrashReport(CrashReportTag.ALERT, CrashReportTrigger.UI, message = alert.name)
    }
}
