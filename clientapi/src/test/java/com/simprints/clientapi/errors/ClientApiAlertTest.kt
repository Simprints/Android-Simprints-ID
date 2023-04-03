package com.simprints.clientapi.errors

import com.google.common.truth.Truth.assertThat
import com.simprints.clientapi.R
import com.simprints.clientapi.errors.ClientApiAlert.*
import com.simprints.clientapi.errors.ClientApiAlert.Companion.toAlertConfig
import com.simprints.feature.alert.config.AlertColor.Red
import com.simprints.feature.alert.config.AlertColor.Yellow
import org.junit.Test

class ClientApiAlertTest {

    @Test
    fun setsCorrectColorForErrors() = mapOf(
        INVALID_STATE_FOR_INTENT_ACTION to Yellow,
        INVALID_METADATA to Yellow,
        INVALID_MODULE_ID to Yellow,
        INVALID_PROJECT_ID to Yellow,
        INVALID_SELECTED_ID to Yellow,
        INVALID_SESSION_ID to Yellow,
        INVALID_USER_ID to Yellow,
        INVALID_VERIFY_ID to Yellow,
        ROOTED_DEVICE to Red,
    ).forEach { (alert, color) -> assertThat(alert.toAlertConfig().color).isEqualTo(color) }

    @Test
    fun setsCorrectTitleForErrors() = mapOf(
        INVALID_STATE_FOR_INTENT_ACTION to R.string.configuration_error_title,
        INVALID_METADATA to R.string.configuration_error_title,
        INVALID_MODULE_ID to R.string.configuration_error_title,
        INVALID_PROJECT_ID to R.string.configuration_error_title,
        INVALID_SELECTED_ID to R.string.configuration_error_title,
        INVALID_SESSION_ID to R.string.configuration_error_title,
        INVALID_USER_ID to R.string.configuration_error_title,
        INVALID_VERIFY_ID to R.string.configuration_error_title,
        ROOTED_DEVICE to R.string.rooted_device_title,
    ).forEach { (alert, color) -> assertThat(alert.toAlertConfig().titleRes).isEqualTo(color) }

    @Test
    fun setsMessageIconOnlyForRootedDevice() = mapOf(
        INVALID_STATE_FOR_INTENT_ACTION to false,
        INVALID_METADATA to false,
        INVALID_MODULE_ID to false,
        INVALID_PROJECT_ID to false,
        INVALID_SELECTED_ID to false,
        INVALID_SESSION_ID to false,
        INVALID_USER_ID to false,
        INVALID_VERIFY_ID to false,
        ROOTED_DEVICE to true,
    ).forEach { (alert, hasIcon) -> assertThat(alert.toAlertConfig().messageIcon != null).isEqualTo(hasIcon) }
}
