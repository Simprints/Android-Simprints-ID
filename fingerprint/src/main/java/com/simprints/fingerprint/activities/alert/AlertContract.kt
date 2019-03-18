package com.simprints.fingerprint.activities.alert

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.simprints.fingerprint.activities.BasePresenter
import com.simprints.fingerprint.activities.BaseView
import com.simprints.fingerprint.data.domain.alert.FingerprintAlert

interface AlertContract {

    interface View : BaseView<Presenter> {
        @ColorInt fun getColorForColorRes(colorRes: Int): Int
        fun setLayoutBackgroundColor(@ColorInt color: Int)
        fun setLeftButtonBackgroundColor(@ColorInt color: Int)
        fun setRightButtonBackgroundColor(@ColorInt color: Int)
        fun setAlertTitleWithStringRes(@StringRes stringRes: Int)
        fun setAlertImageWithDrawableId(@DrawableRes drawableId: Int)
        fun setAlertHintImageWithDrawableId(@DrawableRes alertHintDrawableId: Int?)
        fun initLeftButton(leftButtonAction: FingerprintAlert.ButtonAction)
        fun initRightButton(rightButtonAction: FingerprintAlert.ButtonAction)
        fun setAlertMessageWithStringRes(@StringRes stringRes: Int)
        fun setResult(resultCode: Int)
        fun openBluetoothSettings()
        fun openWifiSettings()
        fun closeActivity()
        fun closeAllActivities()
    }

    interface Presenter : BasePresenter {
        fun handleButtonClick(buttonAction: FingerprintAlert.ButtonAction)
        fun handleBackButton()
    }
}
