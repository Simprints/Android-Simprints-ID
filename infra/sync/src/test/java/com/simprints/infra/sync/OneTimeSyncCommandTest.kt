package com.simprints.infra.sync

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class OneTimeSyncCommandTest {
    @Test
    fun `events start builds expected command`() {
        assertThat(OneTime.Events.start())
            .isEqualTo(OneTime.EventsCommand(action = OneTime.Action.START, isDownSyncAllowed = true))

        assertThat(OneTime.Events.start(isDownSyncAllowed = false))
            .isEqualTo(OneTime.EventsCommand(action = OneTime.Action.START, isDownSyncAllowed = false))
    }

    @Test
    fun `events stop builds expected command`() {
        assertThat(OneTime.Events.stop())
            .isEqualTo(OneTime.EventsCommand(action = OneTime.Action.STOP, isDownSyncAllowed = true))
    }

    @Test
    fun `events restart builds expected command`() {
        assertThat(OneTime.Events.restart())
            .isEqualTo(OneTime.EventsCommand(action = OneTime.Action.RESTART, isDownSyncAllowed = true))

        assertThat(OneTime.Events.restart(isDownSyncAllowed = false))
            .isEqualTo(OneTime.EventsCommand(action = OneTime.Action.RESTART, isDownSyncAllowed = false))
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
