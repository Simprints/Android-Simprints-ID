package com.simprints.id.domain.alert

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.Keep
import androidx.annotation.StringRes
import com.simprints.id.R

//TODO: originally in the pro-guard. Do we really need it unobfuscated?
@Keep
enum class AlertActivityViewModel(val type: Type,
                                  val leftButton: ButtonAction,
                                  val rightButton: ButtonAction,
                                  @StringRes val message: Int) {

    DIFFERENT_PROJECT_ID(
        type = Type.ConfigurationError(hintDrawable = R.drawable.error_hint_key),
        leftButton = ButtonAction.None,
        rightButton = ButtonAction.Close,
        message = R.string.different_projectId_message
    ),

    DIFFERENT_USER_ID(
        type = Type.ConfigurationError(hintDrawable = R.drawable.error_hint_key),
        leftButton = ButtonAction.None,
        rightButton = ButtonAction.Close,
        message = R.string.different_userId_message
    ),

    SAFETYNET_DOWN(
        type = Type.ConfigurationError(title = R.string.alert_try_again_soon,
            mainDrawable = R.drawable.error_hint_wifi),
        leftButton = ButtonAction.None,
        rightButton = ButtonAction.Close,
        message = R.string.safetynet_down_alert_message
    ),

    UNEXPECTED_ERROR(
        type = Type.UnexpectedError(),
        leftButton = ButtonAction.Close,
        rightButton = ButtonAction.None,
        message = R.string.unforeseen_error_message
    );

    companion object {
        fun fromAlertToAlertViewModel(alertType: AlertType) =
            when (alertType) {
                AlertType.DIFFERENT_PROJECT_ID_SIGNED_IN -> DIFFERENT_PROJECT_ID
                AlertType.DIFFERENT_USER_ID_SIGNED_IN -> DIFFERENT_USER_ID
                AlertType.UNEXPECTED_ERROR -> UNEXPECTED_ERROR
                AlertType.SAFETYNET_DOWN -> SAFETYNET_DOWN
            }
    }

    @StringRes val title: Int = type.title
    @ColorRes val backgroundColor: Int = type.backgroundColor
    @DrawableRes val mainDrawable: Int = type.mainDrawable
    @DrawableRes val hintDrawable: Int? = type.hintDrawable

    @Keep
    sealed class Type(@StringRes val title: Int,
                      @ColorRes val backgroundColor: Int,
                      @DrawableRes val mainDrawable: Int,
                      @DrawableRes val hintDrawable: Int? = null) {

        @Keep
        class ConfigurationError(title: Int = R.string.configuration_error_title,
                                 backgroundColor: Int = R.color.simprints_yellow,
                                 mainDrawable: Int = R.drawable.error_icon,
                                 hintDrawable: Int? = null)
            : Type(title, backgroundColor, mainDrawable, hintDrawable)

        @Keep
        class UnexpectedError(title: Int = R.string.error_occurred_title,
                              backgroundColor: Int = R.color.simprints_red,
                              mainDrawable: Int = R.drawable.error_icon,
                              hintDrawable: Int? = null)
            : Type(title, backgroundColor, mainDrawable, hintDrawable)
    }

    @Keep
    sealed class ButtonAction(@StringRes val buttonText: Int = R.string.empty) {
        object None : ButtonAction()
        object Close : ButtonAction(R.string.close)
    }
}
