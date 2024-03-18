package com.simprints.face.capture.screens.controller

import androidx.annotation.Keep
import com.simprints.core.domain.response.AppErrorReason
import com.simprints.feature.alert.alertConfiguration
import com.simprints.feature.alert.config.AlertButtonConfig
import com.simprints.feature.alert.config.AlertColor
import com.simprints.feature.alert.toArgs
import com.simprints.infra.events.event.domain.models.AlertScreenEvent
import com.simprints.infra.resources.R
import com.simprints.infra.uibase.annotations.ExcludedFromGeneratedTestCoverageReports

@Keep
@ExcludedFromGeneratedTestCoverageReports("Alert screen configuration")
internal object InvalidFaceLicenseAlert {
    fun toAlertArgs() = alertConfiguration {
        color = AlertColor.Gray
        titleRes = R.string.configuration_licence_invalid_title
        messageRes = R.string.configuration_licence_invalid_message
        image = R.drawable.ic_exclamation
        leftButton = AlertButtonConfig.Close
        appErrorReason = AppErrorReason.FACE_LICENSE_INVALID
        eventType = AlertScreenEvent.AlertScreenPayload.AlertScreenEventType.FACE_LICENSE_INVALID
    }.toArgs()
}
