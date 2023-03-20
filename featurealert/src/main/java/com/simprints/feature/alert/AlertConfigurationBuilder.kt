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
