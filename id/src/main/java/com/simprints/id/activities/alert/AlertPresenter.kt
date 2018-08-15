package com.simprints.id.activities.alert

import android.app.Activity.RESULT_CANCELED
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.analytics.events.SessionEventsManager
import com.simprints.id.data.analytics.events.models.AlertScreenEvent
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.di.AppComponent
import com.simprints.id.domain.ALERT_TYPE
import com.simprints.id.tools.TimeHelper
import javax.inject.Inject

class AlertPresenter(val view: AlertContract.View,
                     val component: AppComponent,
                     val alertType: ALERT_TYPE) : AlertContract.Presenter {

    @Inject lateinit var analyticsManager: AnalyticsManager
    @Inject lateinit var sessionManager: SessionEventsManager
    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var timeHelper: TimeHelper

    init {
        component.inject(this)
    }

    override fun start() {
        analyticsManager.logAlert(alertType)
        checkAlertTypeAndHandleButtons()
        val color = view.getColorForColorRes(alertType.backgroundColor)
        view.setLayoutBackgroundColor(color)
        view.setLeftButtonBackgroundColor(color)
        view.setRightButtonBackgroundColor(color)
        view.setAlertTitleWithStringRes(alertType.alertTitleId)
        view.setAlertImageWithDrawableId(alertType.alertMainDrawableId)
        view.setAlertHintImageWithDrawableId(alertType.alertHintDrawableId)
        view.setAlertMessageWithStringRes(alertType.alertMessageId)

        sessionManager.updateSessionInBackground({
            it.events.add(AlertScreenEvent(it.nowRelativeToStartTime(timeHelper), alertType))
        })
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
                view.closeAllActivities()
            }
        }
    }

    override fun handleBackButton() {
        view.setResult(RESULT_CANCELED)
    }

    private fun checkAlertTypeAndHandleButtons() {
        if (alertType.isTwoButton) {
            view.initLeftButton(alertType)
        } else {
            view.hideLeftButton()
        }
        view.initRightButton(alertType)
    }
}
