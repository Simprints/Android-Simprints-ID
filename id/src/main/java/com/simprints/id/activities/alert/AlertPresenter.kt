package com.simprints.id.activities.alert

import android.app.Activity.RESULT_CANCELED
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.crashreport.CrashReportTag
import com.simprints.id.data.analytics.crashreport.CrashReportTrigger
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.analytics.eventdata.models.domain.events.AlertScreenEvent
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.di.AppComponent
import com.simprints.id.domain.alert.Alert
import com.simprints.id.tools.TimeHelper
import javax.inject.Inject

class AlertPresenter(val view: AlertContract.View,
                     val component: AppComponent,
                     val alert: Alert) : AlertContract.Presenter {

    @Inject lateinit var crashReportManager: CrashReportManager
    @Inject lateinit var sessionManager: SessionEventsManager
    @Inject lateinit var preferencesManager: PreferencesManager
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
            it.addEvent(AlertScreenEvent(timeHelper.now(), alert))
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

    override fun handleButtonClick(buttonAction: Alert.ButtonAction) {
        buttonAction.resultCode?.let { view.setResult(it) }
        when (buttonAction) {
            is Alert.ButtonAction.None -> Unit
            is Alert.ButtonAction.WifiSettings -> view.openWifiSettings()
            is Alert.ButtonAction.BluetoothSettings -> view.openBluetoothSettings()
            is Alert.ButtonAction.TryAgain -> view.closeActivity()
            is Alert.ButtonAction.Close -> view.closeAllActivities()
        }
    }

    override fun handleBackButton() {
        view.setResult(RESULT_CANCELED)
    }

    private fun logToCrashReport() {
        crashReportManager.logMessageForCrashReport(CrashReportTag.ALERT, CrashReportTrigger.UI, message = alert.name)
    }
}
