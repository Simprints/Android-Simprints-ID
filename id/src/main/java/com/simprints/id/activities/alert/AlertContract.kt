package com.simprints.id.activities.alert

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.simprints.id.activities.BasePresenter
import com.simprints.id.activities.BaseView
import com.simprints.id.domain.alert.AlertViewModel

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
        fun initLeftButton(leftButtonAction: AlertViewModel.ButtonAction)
        fun initRightButton(rightButtonAction: AlertViewModel.ButtonAction)
        fun setAlertMessageWithStringRes(@StringRes stringRes: Int)
        fun setResult(resultCode: Int)
        fun closeActivityAfterCloseButton()
    }

    interface Presenter : BasePresenter {
        fun handleButtonClick(buttonAction: AlertViewModel.ButtonAction)
        fun handleBackButton()
    }
}
