package com.simprints.face.error

import androidx.annotation.Keep
import com.simprints.feature.alert.AlertContract
import com.simprints.feature.alert.alertConfiguration
import com.simprints.feature.alert.config.AlertButtonConfig
import com.simprints.feature.alert.config.AlertColor
import com.simprints.feature.alert.toArgs
import com.simprints.infra.events.event.domain.models.AlertScreenEvent
import com.simprints.infra.uibase.annotations.ExcludedFromGeneratedTestCoverageReports
import com.simprints.infra.resources.R as IDR

@Keep
@ExcludedFromGeneratedTestCoverageReports("Alert screen configuration")
object ErrorType {

    fun toAlertConfiguration() = alertConfiguration {
        color = AlertColor.Red
        titleRes = IDR.string.error_unexpected_error_title
        messageRes = IDR.string.error_unexpected_error_message
        image = IDR.drawable.ic_exclamation
        leftButton = AlertButtonConfig(
            text = null,
            textRes = IDR.string.close,
            resultKey = AlertContract.ALERT_BUTTON_PRESSED_BACK,
            closeOnClick = true,
        )
        eventType = AlertScreenEvent.AlertScreenPayload.AlertScreenEventType.UNEXPECTED_ERROR
    }.toArgs()
}
