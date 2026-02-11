package com.simprints.infra.sync

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ImageSyncStatusTest {
    @Test
    fun `nonNegativeProgress returns zeros when progress is missing`() {
        val status = ImageSyncStatus(
            isSyncing = false,
            progress = null,
            lastUpdateTimeMillis = null,
        )

        assertThat(status.nonNegativeProgress).isEqualTo(0 to 0)
    }

    @Test
    fun `nonNegativeProgress clamps negative values`() {
        val status = ImageSyncStatus(
            isSyncing = true,
            progress = -1 to -5,
            lastUpdateTimeMillis = null,
        )

        assertThat(status.nonNegativeProgress).isEqualTo(0 to 0)
    }

    @Test
    fun `normalizedProgressProportion returns zero when image sync is not running`() {
        val status = ImageSyncStatus(
            isSyncing = false,
            progress = 3 to 10,
            lastUpdateTimeMillis = null,
        )

        assertThat(status.normalizedProgressProportion).isEqualTo(0f)
    }

    @Test
    fun `normalizedProgressProportion returns one when syncing and total is zero`() {
        val status = ImageSyncStatus(
            isSyncing = true,
            progress = 5 to 0,
            lastUpdateTimeMillis = null,
        )

        assertThat(status.normalizedProgressProportion).isEqualTo(1f)
    }

    @Test
    fun `normalizedProgressProportion returns ratio clamped to one`() {
        val status = ImageSyncStatus(
            isSyncing = true,
            progress = 25 to 10,
            lastUpdateTimeMillis = null,
        )

        assertThat(status.normalizedProgressProportion).isEqualTo(1f)
    }

    @Test
    fun `normalizedProgressProportion returns ratio when syncing with positive total`() {
        val status = ImageSyncStatus(
            isSyncing = true,
            progress = 3 to 10,
            lastUpdateTimeMillis = null,
        )

        assertThat(status.normalizedProgressProportion).isEqualTo(0.3f)
    }
}
