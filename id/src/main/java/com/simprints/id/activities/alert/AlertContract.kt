package com.simprints.id.activities.alert

import com.simprints.id.activities.BasePresenter
import com.simprints.id.activities.BaseView
import com.simprints.id.domain.ALERT_TYPE

interface AlertContract {

    interface View : BaseView<Presenter> {
        fun getColorForColorRes(colorRes: Int): Int
        fun setLayoutBackgroundColor(color: Int)
        fun setLeftButtonBackgroundColor(color: Int)
        fun setRightButtonBackgroundColor(color: Int)
        fun setAlertTitleWithStringRes(stringRes: Int)
        fun setAlertImageWithDrawableId(drawableId: Int)
        fun setAlertHintImageWithDrawableId(alertHintDrawableId: Int)
        fun initLeftButton(alertType: ALERT_TYPE)
        fun setAlertMessageWithStringRes(stringRes: Int)
        fun initRightButton(alertType: ALERT_TYPE)
        fun setResult(resultCode: Int)
        fun openBluetoothSettings()
        fun openWifiSettings()
        fun closeActivity()
        fun closeAllActivities()
    }

    interface Presenter : BasePresenter {
        fun handleLeftButtonClick()
        fun handleRightButtonClick()
        fun handleBackButton()
    }
}
