package com.simprints.fingerprint.activities.alert

import com.simprints.fingerprint.activities.alert.AlertActivityViewModel.ButtonAction.*
import com.simprints.fingerprint.activities.alert.FingerprintAlert.*
import com.simprints.fingerprint.activities.alert.result.AlertTaskResult.CloseButtonAction.*
import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportManager
import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportTag.ALERT
import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportTrigger.UI
import com.simprints.fingerprint.controllers.core.eventData.FingerprintSessionEventsManager
import com.simprints.fingerprint.controllers.core.eventData.model.AlertScreenEvent
import com.simprints.fingerprint.controllers.core.timehelper.FingerprintTimeHelper
import com.simprints.fingerprint.di.FingerprintComponent
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class AlertPresenter(val view: AlertContract.View,
                     val component: FingerprintComponent,
                     private val alertType: FingerprintAlert) : AlertContract.Presenter {

    private val alertViewModel =  AlertActivityViewModel.fromAlertToAlertViewModel(alertType)

    @Inject lateinit var crashReportManager: FingerprintCrashReportManager
    @Inject lateinit var sessionManager: FingerprintSessionEventsManager
    @Inject lateinit var timeHelper: FingerprintTimeHelper

    private val settingsOpenedForPairing = AtomicBoolean(false)

    init {
        component.inject(this)
    }

    override fun start() {
        logToCrashReport()

        initButtons()
        initColours()
        initTextAndDrawables()

        sessionManager.addEventInBackground(AlertScreenEvent(timeHelper.now(), alertType))
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

    override fun handleButtonClick(buttonAction: AlertActivityViewModel.ButtonAction) {
        when (buttonAction) {
            is None -> Unit
            is WifiSettings -> view.openWifiSettings()
            is BluetoothSettings -> view.openBluetoothSettings()
            is TryAgain -> view.finishWithAction(TRY_AGAIN)
            is Close -> finishWithCloseActionExceptForLowBatteryAlert()
            is PairScanner -> {
                view.openBluetoothSettings()
                settingsOpenedForPairing.set(true)
            }
        }
    }

    private fun finishWithCloseActionExceptForLowBatteryAlert() {
        if (alertType == LOW_BATTERY) {
            view.startRefusalActivity()
        } else {
            view.finishWithAction(CLOSE)
        }
    }

    override fun handleBackPressed() {
        if (alertType == UNEXPECTED_ERROR || alertType == GUID_NOT_FOUND_ONLINE) {
            view.finishWithAction(BACK)
        } else {
            view.startRefusalActivity()
        }
    }

    override fun handleOnResume() {
        if(settingsOpenedForPairing.getAndSet(false)) {
            view.finishWithAction(TRY_AGAIN)
        }
    }

    private fun logToCrashReport() {
        crashReportManager.logMessageForCrashReport(ALERT, UI, message = alertViewModel.name)
    }
}
