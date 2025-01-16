package com.simprints.feature.orchestrator.usecases

import com.google.common.truth.Truth.assertThat
import org.junit.Test

internal class FaceAutoCaptureEligibilityUseCaseTest {

    private val deviceId = "test_device"
    private val useCase = FaceAutoCaptureEligibilityUseCase(deviceId)

    @Test
    fun `should return false when config is null`() {
        assertThat(useCase(null)).isFalse()
    }

    @Test
    fun `should return false when config is empty`() {
        assertThat(useCase(emptyMap<String, Any>())).isFalse()
    }

    @Test
    fun `should return false when enabled flag is false`() {
        val config = mapOf(
            "enabled" to false,
        )
        assertThat(useCase(config)).isFalse()
    }

    @Test
    fun `should return false when enabled flag is invalid type`() {
        val config = mapOf(
            "enabled" to "not_a_boolean",
        )
        assertThat(useCase(config)).isFalse()
    }

    @Test
    fun `should return true when enabled is true and no device filter`() {
        val config = mapOf(
            "enabled" to true,
        )
        assertThat(useCase(config)).isTrue()
    }

    @Test
    fun `should return true when enabled is true and device is in filter`() {
        val config = mapOf(
            "enabled" to true,
            "deviceIdFilter" to listOf(deviceId, "other_device"),
        )
        assertThat(useCase(config)).isTrue()
    }

    @Test
    fun `should return false when enabled is true but device not in filter`() {
        val config = mapOf(
            "enabled" to true,
            "deviceIdFilter" to listOf("other_device"),
        )
        assertThat(useCase(config)).isFalse()
    }

    @Test
    fun `should return false when enabled is true but filter is empty`() {
        val config = mapOf(
            "enabled" to true,
            "deviceIdFilter" to emptyList<String>(),
        )
        assertThat(useCase(config)).isFalse()
    }

    @Test
    fun `should return false when enabled is true but filter is incorrect type`() {
        val config = mapOf(
            "enabled" to true,
            "deviceIdFilter" to "not_a_list",
        )
        assertThat(useCase(config)).isFalse()
    }

}
