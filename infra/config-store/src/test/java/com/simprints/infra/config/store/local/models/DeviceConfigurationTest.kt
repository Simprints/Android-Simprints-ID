package com.simprints.infra.config.store.local.models

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.store.testtools.deviceConfiguration
import com.simprints.infra.config.store.testtools.protoDeviceConfiguration
import org.junit.Test

class DeviceConfigurationTest {
    @Test
    fun `should map correctly the model`() {
        assertThat(protoDeviceConfiguration.toDomain()).isEqualTo(deviceConfiguration)
        assertThat(deviceConfiguration.toProto()).isEqualTo(protoDeviceConfiguration)
    }
}
