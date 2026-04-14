package com.simprints.infra.sync

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ScheduleSyncCommandTest {
    @Test
    fun `everything commands build expected command`() {
        assertThat(ScheduleCommand.Everything.reschedule())
            .isEqualTo(ScheduleCommand.EverythingCommand(action = ScheduleCommand.Action.RESCHEDULE, withDelay = false))

        assertThat(ScheduleCommand.Everything.reschedule(withDelay = true))
            .isEqualTo(ScheduleCommand.EverythingCommand(action = ScheduleCommand.Action.RESCHEDULE, withDelay = true))

        assertThat(ScheduleCommand.Everything.unschedule())
            .isEqualTo(ScheduleCommand.EverythingCommand(action = ScheduleCommand.Action.UNSCHEDULE))
    }

    @Test
    fun `up sync commands build expected command`() {
        assertThat(ScheduleCommand.UpSync.reschedule())
            .isEqualTo(ScheduleCommand.UpSyncCommand(action = ScheduleCommand.Action.RESCHEDULE, withDelay = false))

        assertThat(ScheduleCommand.UpSync.reschedule(withDelay = true))
            .isEqualTo(ScheduleCommand.UpSyncCommand(action = ScheduleCommand.Action.RESCHEDULE, withDelay = true))

        assertThat(ScheduleCommand.UpSync.unschedule())
            .isEqualTo(ScheduleCommand.UpSyncCommand(action = ScheduleCommand.Action.UNSCHEDULE))
    }

    @Test
    fun `down sync commands build expected command`() {
        assertThat(ScheduleCommand.DownSync.reschedule())
            .isEqualTo(ScheduleCommand.DownSyncCommand(action = ScheduleCommand.Action.RESCHEDULE, withDelay = false))

        assertThat(ScheduleCommand.DownSync.reschedule(withDelay = true))
            .isEqualTo(ScheduleCommand.DownSyncCommand(action = ScheduleCommand.Action.RESCHEDULE, withDelay = true))

        assertThat(ScheduleCommand.DownSync.unschedule())
            .isEqualTo(ScheduleCommand.DownSyncCommand(action = ScheduleCommand.Action.UNSCHEDULE))
    }

    @Test
    fun `images commands build expected command`() {
        assertThat(ScheduleCommand.Images.reschedule())
            .isEqualTo(ScheduleCommand.ImagesCommand(action = ScheduleCommand.Action.RESCHEDULE))

        assertThat(ScheduleCommand.Images.unschedule())
            .isEqualTo(ScheduleCommand.ImagesCommand(action = ScheduleCommand.Action.UNSCHEDULE))
    }
}
