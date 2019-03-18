package com.simprints.fingerprint.activities.alert

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.simprints.id.activities.BasePresenter
import com.simprints.id.activities.BaseView
import com.simprints.id.domain.alert.Alert

interface AlertContract {

    interface View : BaseView<Presenter> {
        @ColorInt fun getColorForColorRes(colorRes: Int): Int
        fun setLayoutBackgroundColor(@ColorInt color: Int)
        fun setLeftButtonBackgroundColor(@ColorInt color: Int)
        fun setRightButtonBackgroundColor(@ColorInt color: Int)
        fun setAlertTitleWithStringRes(@StringRes stringRes: Int)
        fun setAlertImageWithDrawableId(@DrawableRes drawableId: Int)
        fun setAlertHintImageWithDrawableId(@DrawableRes alertHintDrawableId: Int?)
        fun initLeftButton(leftButtonAction: Alert.ButtonAction)
        fun initRightButton(rightButtonAction: Alert.ButtonAction)
        fun setAlertMessageWithStringRes(@StringRes stringRes: Int)
        fun setResult(resultCode: Int)
        fun openBluetoothSettings()
        fun openWifiSettings()
        fun closeActivity()
        fun closeAllActivities()
    }

    interface Presenter : BasePresenter {
        fun handleButtonClick(buttonAction: Alert.ButtonAction)
        fun handleBackButton()
    }
}
