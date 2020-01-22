package com.simprints.clientapi.activities.errors

import androidx.annotation.ColorRes
import com.simprints.clientapi.activities.BasePresenter
import com.simprints.clientapi.activities.BaseView

interface ErrorContract {

    interface View : BaseView<Presenter> {
        fun setErrorTitleText(title: String)
        fun setErrorMessageText(message: String)
        fun setBackgroundColour(colour: Int)
        fun closeActivity()
        fun getStringFromResources(res: Int): String
        fun getColourFromResources(@ColorRes colourId: Int): Int
        fun setErrorHintVisible(isHintVisible: Boolean)
    }

    interface Presenter : BasePresenter {
        fun start(clientApiAlert: ClientApiAlert)
        fun handleCloseOrBackClick()
    }

}
