package com.simprints.fingerprint.capture.extensions

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.store.models.Vero2Configuration.ImageSavingStrategy.EAGER
import com.simprints.infra.config.store.models.Vero2Configuration.ImageSavingStrategy.NEVER
import com.simprints.infra.config.store.models.Vero2Configuration.ImageSavingStrategy.ONLY_GOOD_SCAN
import com.simprints.infra.config.store.models.Vero2Configuration.ImageSavingStrategy.ONLY_USED_IN_REFERENCE
import org.junit.Test

internal class SaveFingerprintImagesStrategyTest {
    @Test
    fun deduceFileExtension() {
        mapOf(
            EAGER to "wsq",
            ONLY_GOOD_SCAN to "wsq",
            ONLY_USED_IN_REFERENCE to "wsq",
            NEVER to "",
        ).map { (actual, expected) -> assertThat(actual.deduceFileExtension()).isEqualTo(expected) }
    }

    @Test
    fun isImageTransferRequired() {
        mapOf(
            EAGER to true,
            ONLY_GOOD_SCAN to true,
            ONLY_USED_IN_REFERENCE to true,
            NEVER to false,
        ).map { (actual, expected) -> assertThat(actual.isImageTransferRequired()).isEqualTo(expected) }
    }

    @Test
    fun isEager() {
        mapOf(
            EAGER to true,
            ONLY_GOOD_SCAN to false,
            ONLY_USED_IN_REFERENCE to false,
            NEVER to false,
        ).map { (actual, expected) -> assertThat(actual.isEager()).isEqualTo(expected) }
    }
}
