package com.simprints.fingerprint.activities.alert

import com.simprints.fingerprint.activities.alert.AlertActivityViewModel.ButtonAction.*
import com.simprints.fingerprint.activities.alert.FingerprintAlert.LOW_BATTERY
import com.simprints.fingerprint.activities.alert.FingerprintAlert.UNEXPECTED_ERROR
import com.simprints.fingerprint.activities.alert.result.AlertTaskResult.CloseButtonAction.*
import com.simprints.fingerprint.controllers.core.eventData.FingerprintSessionEventsManager
import com.simprints.fingerprint.controllers.core.eventData.model.AlertScreenEvent
import com.simprints.fingerprint.controllers.core.timehelper.FingerprintTimeHelper
import com.simprints.infra.logging.LoggingConstants.CrashReportTag
import com.simprints.infra.logging.Simber
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.atomic.AtomicBoolean

class AlertPresenter @AssistedInject constructor(
    @Assisted private val view: AlertContract.View,
    @Assisted private val alertType: FingerprintAlert,
    private val sessionManager: FingerprintSessionEventsManager,
    private val timeHelper: FingerprintTimeHelper,
) : AlertContract.Presenter {

    private val alertViewModel = AlertActivityViewModel.fromAlertToAlertViewModel(alertType)

    private val settingsOpenedForPairing = AtomicBoolean(false)

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
        if (alertType == UNEXPECTED_ERROR) {
            view.finishWithAction(BACK)
        } else {
            view.startRefusalActivity()
        }
    }

    override fun handleOnResume() {
        if (settingsOpenedForPairing.getAndSet(false)) {
            view.finishWithAction(TRY_AGAIN)
        }
    }

    private fun logToCrashReport() {
        Simber.tag(CrashReportTag.ALERT.name).i(alertViewModel.name)
    }
}
