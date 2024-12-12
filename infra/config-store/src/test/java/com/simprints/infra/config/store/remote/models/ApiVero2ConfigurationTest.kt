package com.simprints.infra.config.store.remote.models

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.store.models.Vero2Configuration
import com.simprints.infra.config.store.testtools.apiVero2Configuration
import com.simprints.infra.config.store.testtools.vero2Configuration
import org.junit.Test

class ApiVero2ConfigurationTest {
    @Test
    fun `should map correctly the model`() {
        assertThat(apiVero2Configuration.toDomain()).isEqualTo(vero2Configuration)
    }

    @Test
    fun `should map correctly the ImageSavingStrategy enums`() {
        val mapping = mapOf(
            ApiVero2Configuration.ImageSavingStrategy.NEVER to Vero2Configuration.ImageSavingStrategy.NEVER,
            ApiVero2Configuration.ImageSavingStrategy.ONLY_GOOD_SCAN to Vero2Configuration.ImageSavingStrategy.ONLY_GOOD_SCAN,
            ApiVero2Configuration.ImageSavingStrategy.ONLY_USED_IN_REFERENCE to
                Vero2Configuration.ImageSavingStrategy.ONLY_USED_IN_REFERENCE,
            ApiVero2Configuration.ImageSavingStrategy.EAGER to Vero2Configuration.ImageSavingStrategy.EAGER,
        )

        mapping.forEach {
            assertThat(it.key.toDomain()).isEqualTo(it.value)
        }
    }

    @Test
    fun `should map correctly the CaptureStrategy enums`() {
        val mapping = mapOf(
            ApiVero2Configuration.CaptureStrategy.SECUGEN_ISO_500_DPI to Vero2Configuration.CaptureStrategy.SECUGEN_ISO_500_DPI,
            ApiVero2Configuration.CaptureStrategy.SECUGEN_ISO_1000_DPI to Vero2Configuration.CaptureStrategy.SECUGEN_ISO_1000_DPI,
            ApiVero2Configuration.CaptureStrategy.SECUGEN_ISO_1300_DPI to Vero2Configuration.CaptureStrategy.SECUGEN_ISO_1300_DPI,
            ApiVero2Configuration.CaptureStrategy.SECUGEN_ISO_1700_DPI to Vero2Configuration.CaptureStrategy.SECUGEN_ISO_1700_DPI,
        )

        mapping.forEach {
            assertThat(it.key.toDomain()).isEqualTo(it.value)
        }
    }
}
