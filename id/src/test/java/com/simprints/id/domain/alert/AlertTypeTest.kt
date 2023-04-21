package com.simprints.id.domain.alert


import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.feature.alert.AlertContract
import com.simprints.id.domain.alert.AlertType.*
import com.simprints.id.domain.alert.AlertType.Companion.fromAlertToAlertTypeEvent
import com.simprints.infra.events.event.domain.models.AlertScreenEvent.AlertScreenPayload.AlertScreenEventType
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AlertTypeTest {

    @Test
    fun alertTypeFromPayload() {
        val alertData = bundleOf(AlertContract.ALERT_PAYLOAD to GUID_NOT_FOUND_ONLINE.toAlertConfig().payload)
        assertThat(AlertType.fromPayload(alertData)).isEqualTo(GUID_NOT_FOUND_ONLINE)
    }

    @Test
    fun defaultAlertTypeFromEmptyPayload() {
        assertThat(AlertType.fromPayload(Bundle())).isEqualTo(UNEXPECTED_ERROR)
    }

    @Test
    fun mapsAlertToEventType() = mapOf(
        DIFFERENT_PROJECT_ID to AlertScreenEventType.DIFFERENT_PROJECT_ID,
        DIFFERENT_USER_ID to AlertScreenEventType.DIFFERENT_USER_ID,
        INTEGRITY_SERVICE_ERROR to AlertScreenEventType.INTEGRITY_SERVICE_ERROR,
        UNEXPECTED_ERROR to AlertScreenEventType.UNEXPECTED_ERROR,
        GUID_NOT_FOUND_ONLINE to AlertScreenEventType.GUID_NOT_FOUND_ONLINE,
        GUID_NOT_FOUND_OFFLINE to AlertScreenEventType.GUID_NOT_FOUND_OFFLINE,
        ENROLMENT_LAST_BIOMETRICS_FAILED to AlertScreenEventType.ENROLMENT_LAST_BIOMETRICS_FAILED,
        GOOGLE_PLAY_SERVICES_OUTDATED to AlertScreenEventType.GOOGLE_PLAY_SERVICES_OUTDATED,
        MISSING_GOOGLE_PLAY_SERVICES to AlertScreenEventType.MISSING_GOOGLE_PLAY_SERVICES,
        MISSING_OR_OUTDATED_GOOGLE_PLAY_STORE_APP to AlertScreenEventType.MISSING_OR_OUTDATED_GOOGLE_PLAY_STORE_APP,
    ).forEach { (alert, eventType) -> assertThat(alert.fromAlertToAlertTypeEvent()).isEqualTo(eventType) }

}
