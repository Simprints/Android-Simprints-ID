package com.simprints.infra.events.event.domain.models

import com.google.common.truth.Truth.*
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.sampledata.SampleDefaults.CREATED_AT
import org.junit.Test

class DeviceConfigurationUpdatedEventTest {
    @Test
    fun create_device_configuration_updated_event() {
        val event = createDefaultEvent()

        with(event.payload) {
            assertThat(createdAt).isEqualTo(CREATED_AT)
            assertThat(configuration.language).isEqualTo("en")
            assertThat(configuration.downSyncModules).containsExactly(
                "module1".asTokenizableRaw(),
                "module2".asTokenizableEncrypted(),
            )
            assertThat(sourceUpdate).isEqualTo(
                DeviceConfigurationUpdatedEvent.DeviceConfigurationUpdateSource.REMOTE,
            )
        }
    }

    @Test
    fun getTokenizableFields_returnsMapWithModuleIds() {
        val event = createDefaultEvent()

        val tokenizableListFields = event.getTokenizableListFields()
        assertThat(tokenizableListFields).hasSize(1)
        assertThat(tokenizableListFields[TokenKeyType.ModuleId]).containsExactly(
            "module1".asTokenizableRaw(),
            "module2".asTokenizableEncrypted(),
        )
    }

    @Test
    fun setTokenizableFields_updatesModuleIds() {
        val event = createDefaultEvent()

        val updatedEvent = event.setTokenizedListFields(
            mapOf(
                TokenKeyType.ModuleId to listOf(
                    "newModule3".asTokenizableEncrypted(),
                    "newModule5".asTokenizableEncrypted(),
                ),
            ),
        ) as DeviceConfigurationUpdatedEvent

        assertThat(updatedEvent.payload.configuration.downSyncModules).containsExactly(
            "newModule3".asTokenizableEncrypted(),
            "newModule5".asTokenizableEncrypted(),
        )
    }

    private fun createDefaultEvent(): DeviceConfigurationUpdatedEvent = DeviceConfigurationUpdatedEvent(
        createdAt = CREATED_AT,
        language = "en",
        downSyncModules = listOf(
            "module1".asTokenizableRaw(),
            "module2".asTokenizableEncrypted(),
        ),
        sourceUpdate = DeviceConfigurationUpdatedEvent.DeviceConfigurationUpdateSource.REMOTE,
    )
}
