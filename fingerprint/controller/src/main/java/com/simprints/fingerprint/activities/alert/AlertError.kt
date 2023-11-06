package com.simprints.fingerprint.activities.alert

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import com.simprints.feature.alert.alertButton
import com.simprints.feature.alert.alertConfiguration
import com.simprints.feature.alert.config.AlertButtonConfig
import com.simprints.feature.alert.config.AlertColor
import com.simprints.fingerprint.activities.alert.AlertError.UNEXPECTED_ERROR
import com.simprints.fingerprint.controllers.core.eventData.model.fromFingerprintAlertToAlertTypeEvent
import com.simprints.infra.resources.R as IDR

/**
 * This class represent the display model for the error alert that occurred while a fingerprint
 * request was being processed.
 *
 * - [UNEXPECTED_ERROR]  an unexpected error occurred while processing fingerprint request
 *
 * @property title  the string resource value representing the tilte of the error
 * @property message  the string resource value to be displayed on the scree
 * @property backgroundColor  the color resource value representing screen's background
 * @property mainDrawable  the image icon to be shown based on the error type
 * @property hintDrawable  the image icon hinting what kind of error occured
 * @property leftButton  the specific action to be triggered when the left button is clicked
 */
enum class AlertError(
    @StringRes val title: Int,
    @StringRes val message: Int,
    val backgroundColor: AlertColor = AlertColor.Default,
    @DrawableRes val mainDrawable: Int,
    @DrawableRes val hintDrawable: Int? = null,
    val leftButton: AlertButtonConfig,
) {

    //Unexpected errors
    UNEXPECTED_ERROR(
        title = IDR.string.error_occurred_title,
        message = IDR.string.unforeseen_error_message,
        backgroundColor = AlertColor.Red,
        mainDrawable = IDR.drawable.ic_alert_default,
        leftButton = Buttons.closeButton(),
    );

    private object Buttons {

        fun closeButton() = alertButton {
            textRes = IDR.string.close
            closeOnClick = true
            resultKey = ACTION_CLOSE
        }
    }

    fun toAlertConfig() = alertConfiguration {
        color = this@AlertError.backgroundColor
        titleRes = this@AlertError.title
        image = this@AlertError.mainDrawable
        messageRes = this@AlertError.message
        messageIcon = this@AlertError.hintDrawable
        eventType = this@AlertError.fromFingerprintAlertToAlertTypeEvent()

        leftButton = this@AlertError.leftButton

        payload = bundleOf(PAYLOAD_KEY to this@AlertError.name)
    }

    companion object {

        internal const val PAYLOAD_KEY = "alert_payload"
        internal const val ACTION_CLOSE = "action_close"
    }

}
