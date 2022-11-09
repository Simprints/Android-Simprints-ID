package com.simprints.fingerprint.activities.alert

import com.google.common.truth.Truth
import com.simprints.fingerprint.activities.alert.FingerprintAlert.*
import org.junit.Test

class AlertErrorTest {

    @Test
    fun fromAlertToAlertViewModel() {
        Truth.assertThat(
            AlertError.fromAlertToAlertError(BLUETOOTH_NOT_SUPPORTED)
        ).isEqualTo(AlertError.BLUETOOTH_NOT_SUPPORTED)

        Truth.assertThat(
            AlertError.fromAlertToAlertError(BLUETOOTH_NOT_ENABLED)
        ).isEqualTo(AlertError.BLUETOOTH_NOT_ENABLED)

        Truth.assertThat(
            AlertError.fromAlertToAlertError(NOT_PAIRED)
        ).isEqualTo(AlertError.NOT_PAIRED)

        Truth.assertThat(
            AlertError.fromAlertToAlertError(MULTIPLE_PAIRED_SCANNERS)
        ).isEqualTo(AlertError.MULTIPLE_PAIRED_SCANNERS)

        Truth.assertThat(
            AlertError.fromAlertToAlertError(DISCONNECTED)
        ).isEqualTo(AlertError.DISCONNECTED)

        Truth.assertThat(
            AlertError.fromAlertToAlertError(LOW_BATTERY)
        ).isEqualTo(AlertError.LOW_BATTERY)

        Truth.assertThat(
            AlertError.fromAlertToAlertError(UNEXPECTED_ERROR)
        ).isEqualTo(AlertError.UNEXPECTED_ERROR)

    }
}
