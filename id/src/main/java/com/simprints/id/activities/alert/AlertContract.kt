package com.simprints.id.activities.alert

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.simprints.id.activities.BasePresenter
import com.simprints.id.activities.BaseView
import com.simprints.id.domain.alert.AlertActivityViewModel

interface AlertContract {

    interface View : BaseView<Presenter> {
        @ColorInt
        fun getColorForColorRes(colorRes: Int): Int

        fun setLayoutBackgroundColor(@ColorInt color: Int)
        fun setLeftButtonBackgroundColor(@ColorInt color: Int)
        fun setRightButtonBackgroundColor(@ColorInt color: Int)
        fun setAlertTitleWithStringRes(@StringRes stringRes: Int)
        fun setAlertImageWithDrawableId(@DrawableRes drawableId: Int)
        fun setAlertHintImageWithDrawableId(@DrawableRes alertHintDrawableId: Int?)
        fun initLeftButton(leftButtonAction: AlertActivityViewModel.ButtonAction)
        fun initRightButton(rightButtonAction: AlertActivityViewModel.ButtonAction)
        fun setAlertMessageWithStringRes(@StringRes stringRes: Int)
        fun closeActivityAfterCloseButton()
        fun startCoreExitFormActivity()
        fun startFingerprintExitFormActivity()
        fun startFaceExitFormActivity()
        fun finishWithTryAgain()
        fun openWifiSettings()
    }

    interface Presenter : BasePresenter {
        fun handleButtonClick(buttonAction: AlertActivityViewModel.ButtonAction)
        fun handleBackButton()
    }
}
