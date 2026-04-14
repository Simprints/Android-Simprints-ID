package com.simprints.infra.sync

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class OneTimeSyncCommandTest {
    @Test
    fun `upSync start builds expected command`() {
        assertThat(OneTime.UpSync.start())
            .isEqualTo(OneTime.UpSyncCommand(action = OneTime.Action.START))
    }

    @Test
    fun `upSync stop builds expected command`() {
        assertThat(OneTime.UpSync.stop())
            .isEqualTo(OneTime.UpSyncCommand(action = OneTime.Action.STOP))
    }

    @Test
    fun `upSync restart builds expected command`() {
        assertThat(OneTime.UpSync.restart())
            .isEqualTo(OneTime.UpSyncCommand(action = OneTime.Action.RESTART))
    }

    @Test
    fun `downSync start builds expected command`() {
        assertThat(OneTime.DownSync.start())
            .isEqualTo(OneTime.DownSyncCommand(action = OneTime.Action.START))
    }

    @Test
    fun `downSync stop builds expected command`() {
        assertThat(OneTime.DownSync.stop())
            .isEqualTo(OneTime.DownSyncCommand(action = OneTime.Action.STOP))
    }

    @Test
    fun `downSync restart builds expected command`() {
        assertThat(OneTime.DownSync.restart())
            .isEqualTo(OneTime.DownSyncCommand(action = OneTime.Action.RESTART))
    }

    @Test
    fun `images commands build expected commands`() {
        assertThat(OneTime.Images.start())
            .isEqualTo(OneTime.ImagesCommand(action = OneTime.Action.START))

        assertThat(OneTime.Images.stop())
            .isEqualTo(OneTime.ImagesCommand(action = OneTime.Action.STOP))

        assertThat(OneTime.Images.restart())
            .isEqualTo(OneTime.ImagesCommand(action = OneTime.Action.RESTART))
    }
}
