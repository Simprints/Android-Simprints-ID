package com.simprints.feature.alert

import android.os.Bundle
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.simprints.feature.alert.config.AlertButtonConfig
import com.simprints.feature.alert.config.AlertColor
import com.simprints.feature.alert.config.AlertConfiguration
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
)

fun alertConfigurationArgs(block: AlertConfigurationBuilder.() -> Unit): Bundle = AlertConfigurationBuilder()
    .apply(block)
    .let { builder ->
        AlertFragmentArgs(AlertConfiguration(
            color = builder.color,
            title = builder.title,
            titleRes = builder.titleRes,
            image = builder.image,
            message = builder.message,
            messageRes = builder.messageRes,
            messageIcon = builder.messageIcon,
            leftButton = builder.leftButton,
            rightButton = builder.rightButton,
        ))
    }
    .toBundle()

fun alertButton(
    text: String? = null,
    @StringRes textRes: Int? = null,
    resultKey: String? = null,
    closeOnClick: Boolean = false,
): AlertButtonConfig = AlertButtonConfig(text, textRes, resultKey, closeOnClick)
