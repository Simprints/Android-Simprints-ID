package com.simprints.fingerprint.activities.alert

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.simprints.fingerprint.activities.alert.AlertContract.Presenter
import com.simprints.fingerprint.activities.alert.AlertContract.View
import com.simprints.fingerprint.activities.alert.result.AlertTaskResult
import com.simprints.fingerprint.activities.base.BasePresenter
import com.simprints.fingerprint.activities.base.BaseView

/**
 * This interface represents the contract between Alert screen [View] and business logic [Presenter].
 */
interface AlertContract {

    interface View : BaseView<Presenter> {
        @ColorInt fun getColorForColorRes(colorRes: Int): Int
        fun setLayoutBackgroundColor(@ColorInt color: Int)
        fun setLeftButtonBackgroundColor(@ColorInt color: Int)
        fun setRightButtonBackgroundColor(@ColorInt color: Int)
        fun setAlertTitleWithStringRes(@StringRes stringRes: Int)
        fun setAlertImageWithDrawableId(@DrawableRes drawableId: Int)
        fun setAlertHintImageWithDrawableId(@DrawableRes alertHintDrawableId: Int?)
        fun initLeftButton(leftButtonAction: AlertError.ButtonAction)
        fun initRightButton(rightButtonAction: AlertError.ButtonAction)
        fun setAlertMessageWithStringRes(@StringRes stringRes: Int)
        fun openBluetoothSettings()
        fun openWifiSettings()
        fun finishWithAction(buttonAction: AlertTaskResult.CloseButtonAction)
        fun startRefusalActivity()
    }

    interface Presenter : BasePresenter {
        fun handleButtonClick(buttonAction: AlertError.ButtonAction)
        fun handleBackPressed()
        fun handleOnResume()
    }
}
