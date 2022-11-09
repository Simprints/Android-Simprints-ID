package com.simprints.fingerprint.activities.alert

import com.simprints.fingerprint.activities.alert.AlertError.ButtonAction.*
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

/**
 * This class implements the presenter for Alert Screen, providing business logic for updating the
 * view.
 *
 * @property view  the view to be updated based on business rules
 * @property sessionManager  the manager for fingerprint session events
 * @property timeHelper  time helper instance for computing time events occur
 */
class AlertPresenter @AssistedInject constructor(
    @Assisted private val view: AlertContract.View,
    @Assisted private val alertType: FingerprintAlert,
    private val sessionManager: FingerprintSessionEventsManager,
    private val timeHelper: FingerprintTimeHelper,
) : AlertContract.Presenter {

    private val alertError =  AlertError.fromAlertToAlertError(alertType)

    private val settingsOpenedForPairing = AtomicBoolean(false)

    override fun start() {
        logToCrashReport()

        initButtons()
        initColours()
        initTextAndDrawables()

        sessionManager.addEventInBackground(AlertScreenEvent(timeHelper.now(), alertType))
    }

    private fun initButtons() {
        view.initLeftButton(alertError.leftButton)
        view.initRightButton(alertError.rightButton)
    }

    private fun initColours() {
        val color = view.getColorForColorRes(alertError.type.backgroundColor)
        view.setLayoutBackgroundColor(color)
        view.setLeftButtonBackgroundColor(color)
        view.setRightButtonBackgroundColor(color)
    }

    private fun initTextAndDrawables() {
        view.setAlertTitleWithStringRes(alertError.type.title)
        view.setAlertImageWithDrawableId(alertError.type.mainDrawable)
        view.setAlertHintImageWithDrawableId(alertError.type.hintDrawable)
        view.setAlertMessageWithStringRes(alertError.message)
    }

    override fun handleButtonClick(buttonAction: AlertError.ButtonAction) {
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
        Simber.tag(CrashReportTag.ALERT.name).i(alertError.name)
    }
}
