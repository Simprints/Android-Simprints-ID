package com.simprints.feature.alert

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.simprints.core.domain.response.AppErrorReason
import com.simprints.feature.alert.config.AlertButtonConfig
import com.simprints.feature.alert.config.AlertColor
import com.simprints.feature.alert.config.AlertConfiguration
import com.simprints.feature.alert.screen.AlertFragmentArgs
import com.simprints.infra.events.event.domain.models.AlertScreenEvent
import com.simprints.infra.resources.R as IDR

data class AlertConfigurationBuilder(
    var color: AlertColor = AlertColor.Default,
    var title: String? = null,
    @StringRes var titleRes: Int? = null,
    @DrawableRes var image: Int = IDR.drawable.ic_alert_default,
    var message: String? = null,
    @StringRes var messageRes: Int? = null,
    @DrawableRes var messageIcon: Int? = null,
    var leftButton: AlertButtonConfig = AlertButtonConfig.Close,
    var rightButton: AlertButtonConfig? = null,
    var eventType: AlertScreenEvent.AlertScreenPayload.AlertScreenEventType? = null,
    var appErrorReason: AppErrorReason? = null,
)

/**
 * Configuration builder for the alert screen:
 *
 * ```
 * color - One of the pre-defined AlertColor values, default - Simprints blue
 * title - Pre-formatted title sting
 * titleRes - Title string resource (used if title is null), default - "Alert"
 * image - Main icon drawable resource id, default - ic_alert_default
 * message - Pre-formatted message sting
 * messageRes - Message string resource (used if message is null), default - empty string
 * messageIcon - Optional icon to show next to the message, default - view is not visible
 * leftButton - Left/main button configuration, default - basic "Close" button
 * rightButton - Optional right button configuration, default - view is not visible
 * appErrorReason - Error code that will be returned in app result if the alert is terminal, default - null
 * eventType - Event type to be logged on alert opening, default - nothing
 * ```
 */
fun alertConfiguration(block: AlertConfigurationBuilder.() -> Unit) = AlertConfigurationBuilder().apply(block)

fun AlertConfigurationBuilder.toArgs() = AlertFragmentArgs(
    AlertConfiguration(
        color = this.color,
        title = this.title,
        titleRes = this.titleRes,
        image = this.image,
        message = this.message,
        messageRes = this.messageRes,
        messageIcon = this.messageIcon,
        leftButton = this.leftButton,
        rightButton = this.rightButton,
        eventType = this.eventType,
        appErrorReason = this.appErrorReason,
    ),
).toBundle()

data class AlertButtonBuilder(
    var text: String? = null,
    @StringRes var textRes: Int? = null,
    var resultKey: String? = null,
    var closeOnClick: Boolean = false,
)

/**
 * Configuration builder for the alert screen button:
 *
 * ```
 * text - Pre-formatted text sting
 * textRes - Text string resource (used if text is null), default - empty string
 * resultKey - Custom action key to distinguish pressed buttons on callers side, default - null
 * closeOnClick - If set to true - alert screen will close itself on button click, default - false
 * ```
 */
fun alertButton(block: AlertButtonBuilder.() -> Unit): AlertButtonConfig = AlertButtonBuilder()
    .apply(block)
    .let { builder ->
        AlertButtonConfig(
            text = builder.text,
            textRes = builder.textRes,
            resultKey = builder.resultKey,
            closeOnClick = builder.closeOnClick,
        )
    }
