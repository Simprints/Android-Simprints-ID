package com.simprints.feature.alert

import android.os.Bundle
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
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
    var payload: Bundle = Bundle(),
)

fun alertConfiguration(block: AlertConfigurationBuilder.() -> Unit) = AlertConfigurationBuilder().apply(block)

fun AlertConfigurationBuilder.withPayload(vararg pairs: Pair<String, Any?>) =
    this.also { it.payload = bundleOf(*pairs) }

fun AlertConfigurationBuilder.toArgs() = AlertFragmentArgs(AlertConfiguration(
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
    payload = this.payload,
)).toBundle()

data class AlertButtonBuilder(
    var text: String? = null,
    @StringRes var textRes: Int? = null,
    var resultKey: String? = null,
    var closeOnClick: Boolean = false,
)

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

