package com.simprints.infra.config.store.local.models

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.store.models.Vero2Configuration
import com.simprints.infra.config.store.testtools.protoVero2Configuration
import com.simprints.infra.config.store.testtools.vero2Configuration
import org.junit.Test

class Vero2ConfigurationTest {
    @Test
    fun `should map correctly the model`() {
        assertThat(protoVero2Configuration.toDomain()).isEqualTo(vero2Configuration)
        assertThat(vero2Configuration.toProto()).isEqualTo(protoVero2Configuration)
    }

    @Test
    fun `should map correctly the ImageSavingStrategy enums`() {
        val mapping = mapOf(
            ProtoVero2Configuration.ImageSavingStrategy.NEVER to Vero2Configuration.ImageSavingStrategy.NEVER,
            ProtoVero2Configuration.ImageSavingStrategy.ONLY_GOOD_SCAN to Vero2Configuration.ImageSavingStrategy.ONLY_GOOD_SCAN,
            ProtoVero2Configuration.ImageSavingStrategy.ONLY_USED_IN_REFERENCE to
                Vero2Configuration.ImageSavingStrategy.ONLY_USED_IN_REFERENCE,
            ProtoVero2Configuration.ImageSavingStrategy.EAGER to Vero2Configuration.ImageSavingStrategy.EAGER,
        )

        mapping.forEach {
            assertThat(it.key.toDomain()).isEqualTo(it.value)
            assertThat(it.value.toProto()).isEqualTo(it.key)
        }
    }

    @Test
    fun `should map correctly the CaptureStrategy enums`() {
        val mapping = mapOf(
            ProtoVero2Configuration.CaptureStrategy.SECUGEN_ISO_500_DPI to Vero2Configuration.CaptureStrategy.SECUGEN_ISO_500_DPI,
            ProtoVero2Configuration.CaptureStrategy.SECUGEN_ISO_1000_DPI to Vero2Configuration.CaptureStrategy.SECUGEN_ISO_1000_DPI,
            ProtoVero2Configuration.CaptureStrategy.SECUGEN_ISO_1300_DPI to Vero2Configuration.CaptureStrategy.SECUGEN_ISO_1300_DPI,
            ProtoVero2Configuration.CaptureStrategy.SECUGEN_ISO_1700_DPI to Vero2Configuration.CaptureStrategy.SECUGEN_ISO_1700_DPI,
        )

        mapping.forEach {
            assertThat(it.key.toDomain()).isEqualTo(it.value)
            assertThat(it.value.toProto()).isEqualTo(it.key)
        }
    }
}
