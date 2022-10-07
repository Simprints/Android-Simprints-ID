package com.simprints.id.domain.alert

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.Keep
import androidx.annotation.StringRes
import com.simprints.id.R
import com.simprints.infra.resources.R as IDR

//TODO: originally in the pro-guard. Do we really need it unobfuscated?
@Keep
enum class AlertActivityViewModel(val type: Type,
                                  val leftButton: ButtonAction,
                                  val rightButton: ButtonAction,
                                  @StringRes val message: Int) {

    GUID_NOT_FOUND_ONLINE(
        type = Type.DataError(
            title = IDR.string.verify_guid_not_found_title,
            hintDrawable = null
        ),
        leftButton = ButtonAction.Close,
        rightButton = ButtonAction.None,
        message = IDR.string.verify_guid_not_found_online_message
    ),

    GUID_NOT_FOUND_OFFLINE(
        type = Type.DataError(
            title = IDR.string.verify_guid_not_found_title,
            hintDrawable = R.drawable.error_hint_wifi
        ),
        leftButton = ButtonAction.TryAgain,
        rightButton = ButtonAction.WifiSettings,
        message = IDR.string.verify_guid_not_found_offline_message
    ),

    DIFFERENT_PROJECT_ID(
        type = Type.ConfigurationError(hintDrawable = R.drawable.error_hint_key),
        leftButton = ButtonAction.Close,
        rightButton = ButtonAction.None,
        message = IDR.string.different_projectId_message
    ),

    DIFFERENT_USER_ID(
        type = Type.ConfigurationError(hintDrawable = R.drawable.error_hint_key),
        leftButton = ButtonAction.Close,
        rightButton = ButtonAction.None,
        message = IDR.string.different_userId_message
    ),

    SAFETYNET_ERROR(
        type = Type.ConfigurationError(title = IDR.string.alert_try_again,
            backgroundColor = IDR.color.simprints_grey,
            mainDrawable = R.drawable.error_icon),
        leftButton = ButtonAction.Close,
        rightButton = ButtonAction.None,
        message = IDR.string.safetynet_down_alert_message
    ),

    UNEXPECTED_ERROR(
        type = Type.UnexpectedError(),
        leftButton = ButtonAction.Close,
        rightButton = ButtonAction.None,
        message = IDR.string.unforeseen_error_message
    ),

    ENROLMENT_LAST_BIOMETRICS_FAILED(
        type = Type.DataError(
            title = IDR.string.enrol_last_biometrics_alert_title,
            hintDrawable = null
        ),
        leftButton = ButtonAction.Close,
        rightButton = ButtonAction.None,
        message = IDR.string.enrol_last_biometrics_alert_message
    ),

    OFFLINE_DURING_MODALITY_INSTALL(
        type = Type.DataError(
            title = IDR.string.modality_installation_offline_title,
            hintDrawable = R.drawable.error_hint_wifi
        ),
        leftButton = ButtonAction.WifiSettingsWithFinish,
        rightButton = ButtonAction.None,
        message = IDR.string.modality_installation_offline
    ),
    MODALITY_DOWNLOAD_CANCELLED(
        type = Type.DataError(
            title = IDR.string.feature_not_downloaded_title
        ),
        leftButton = ButtonAction.TryAgain,
        rightButton = ButtonAction.None,
        message = IDR.string.feature_not_downloaded
    );

    companion object {
        fun fromAlertToAlertViewModel(alertType: AlertType) =
            when (alertType) {
                AlertType.DIFFERENT_PROJECT_ID_SIGNED_IN -> DIFFERENT_PROJECT_ID
                AlertType.DIFFERENT_USER_ID_SIGNED_IN -> DIFFERENT_USER_ID
                AlertType.UNEXPECTED_ERROR -> UNEXPECTED_ERROR
                AlertType.SAFETYNET_ERROR -> SAFETYNET_ERROR
                AlertType.GUID_NOT_FOUND_ONLINE -> GUID_NOT_FOUND_ONLINE
                AlertType.GUID_NOT_FOUND_OFFLINE -> GUID_NOT_FOUND_OFFLINE
                AlertType.ENROLMENT_LAST_BIOMETRICS_FAILED -> ENROLMENT_LAST_BIOMETRICS_FAILED
                AlertType.OFFLINE_DURING_SETUP -> OFFLINE_DURING_MODALITY_INSTALL
                AlertType.SETUP_MODALITY_DOWNLOAD_CANCELLED -> MODALITY_DOWNLOAD_CANCELLED
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
        class DataError(title: Int,
                        backgroundColor: Int = IDR.color.simprints_grey,
                        mainDrawable: Int = R.drawable.error_icon,
                        hintDrawable: Int? = null)
            : Type(title, backgroundColor, mainDrawable, hintDrawable)


        @Keep
        class ConfigurationError(title: Int = IDR.string.configuration_error_title,
                                 backgroundColor: Int = IDR.color.simprints_yellow,
                                 mainDrawable: Int = R.drawable.error_icon,
                                 hintDrawable: Int? = null)
            : Type(title, backgroundColor, mainDrawable, hintDrawable)

        @Keep
        class UnexpectedError(title: Int = IDR.string.error_occurred_title,
                              backgroundColor: Int = IDR.color.simprints_red,
                              mainDrawable: Int = R.drawable.error_icon,
                              hintDrawable: Int? = null)
            : Type(title, backgroundColor, mainDrawable, hintDrawable)
    }

    @Keep
    sealed class ButtonAction(@StringRes val buttonText: Int = IDR.string.empty) {
        object None : ButtonAction()
        object Close : ButtonAction(IDR.string.close)
        object TryAgain : ButtonAction(IDR.string.try_again_label)
        object WifiSettings : ButtonAction(IDR.string.settings_label)
        object WifiSettingsWithFinish : ButtonAction(IDR.string.settings_label)
    }
}
