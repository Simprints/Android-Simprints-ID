package com.simprints.id.activities.alert

import android.app.Activity.RESULT_CANCELED
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.crashreport.CrashReportTag
import com.simprints.id.data.analytics.crashreport.CrashReportTrigger
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.analytics.eventdata.models.domain.events.AlertScreenEvent
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.di.AppComponent
import com.simprints.id.domain.alert.AlertActivityViewModel
import com.simprints.id.domain.alert.AlertType
import com.simprints.id.domain.alert.fromAlertToAlertTypeEvent
import com.simprints.id.tools.TimeHelper
import javax.inject.Inject

class AlertPresenter(val view: AlertContract.View,
                     val component: AppComponent,
                     private val alertTypeType:  AlertType) : AlertContract.Presenter {

    @Inject lateinit var crashReportManager: CrashReportManager
    @Inject lateinit var sessionManager: SessionEventsManager
    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var timeHelper: TimeHelper

    private val alertViewModel = AlertActivityViewModel.fromAlertToAlertViewModel(alertTypeType)

    init {
        component.inject(this)
    }

    override fun start() {
        logToCrashReport()

        initButtons()
        initColours()
        initTextAndDrawables()

        sessionManager.updateSessionInBackground {
            it.addEvent(AlertScreenEvent(timeHelper.now(), alertTypeType.fromAlertToAlertTypeEvent()))
        }
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
            is AlertActivityViewModel.ButtonAction.None -> Unit
            is AlertActivityViewModel.ButtonAction.Close -> view.closeActivityAfterCloseButton()
        }
    }

    override fun handleBackButton() {
        view.closeActivityAfterCloseButton()
    }

    private fun logToCrashReport() {
        crashReportManager.logMessageForCrashReport(CrashReportTag.ALERT, CrashReportTrigger.UI, message = alertViewModel.name)
    }
}
