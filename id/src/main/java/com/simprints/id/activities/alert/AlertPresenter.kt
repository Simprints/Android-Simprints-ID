package com.simprints.id.activities.alert

import android.app.Activity.RESULT_CANCELED
import com.simprints.id.data.DataManager
import com.simprints.id.domain.ALERT_TYPE

class AlertPresenter(val view: AlertContract.View,
                     val dataManager: DataManager,
                     val alertType: ALERT_TYPE) : AlertContract.Presenter {

    override fun start() {
        dataManager.logAlert(alertType)
        val color = view.getColorForColorRes(alertType.backgroundColor)
        view.setLayoutBackgroundColor(color)
        view.setLeftButtonBackgroundColor(color)
        view.setRightButtonBackgroundColor(color)
        view.setAlertTitleWithStringRes(alertType.alertTitleId)
        view.setAlertImageWithDrawableId(alertType.alertMainDrawableId)
        view.setAlertHintImageWithDrawableId(alertType.alertHintDrawableId)
        view.setAlertMessageWithStringRes(alertType.alertMessageId)
        view.initLeftButton(alertType)
        view.initRightButton(alertType)
    }

    override fun handleLeftButtonClick() {
        view.setResult(alertType.resultCode)
        view.closeActivity()
    }

    override fun handleRightButtonClick() {
        when (alertType) {
            ALERT_TYPE.BLUETOOTH_NOT_ENABLED,
            ALERT_TYPE.NOT_PAIRED,
            ALERT_TYPE.MULTIPLE_PAIRED_SCANNERS,
            ALERT_TYPE.DISCONNECTED -> {
                view.openBluetoothSettings()
            }

            ALERT_TYPE.GUID_NOT_FOUND_OFFLINE,
            ALERT_TYPE.UNVERIFIED_API_KEY -> {
                view.openWifiSettings()
            }

            else -> {
                view.setResult(RESULT_CANCELED)
                if (isACriticalError()) {
                    view.closeAllActivities()
                } else {
                    view.closeActivity()
                }
            }
        }
    }

    private fun isACriticalError(): Boolean = alertType == ALERT_TYPE.UNEXPECTED_ERROR

    override fun handleBackButton() {
        view.setResult(RESULT_CANCELED)
    }
}
