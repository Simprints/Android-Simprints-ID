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

        val block: suspend () -> Unit = { }
        val command = ScheduleCommand.Everything.rescheduleAfter(withDelay = true, block = block) as ScheduleCommand.EverythingCommand
        assertThat(command.action).isEqualTo(ScheduleCommand.Action.RESCHEDULE)
        assertThat(command.withDelay).isTrue()
        assertThat(command.blockWhileUnscheduled).isSameInstanceAs(block)
    }

    @Test
    fun `events commands build expected command`() {
        assertThat(ScheduleCommand.Events.reschedule())
            .isEqualTo(ScheduleCommand.EventsCommand(action = ScheduleCommand.Action.RESCHEDULE, withDelay = false))

        assertThat(ScheduleCommand.Events.reschedule(withDelay = true))
            .isEqualTo(ScheduleCommand.EventsCommand(action = ScheduleCommand.Action.RESCHEDULE, withDelay = true))

        assertThat(ScheduleCommand.Events.unschedule())
            .isEqualTo(ScheduleCommand.EventsCommand(action = ScheduleCommand.Action.UNSCHEDULE))

        val block: suspend () -> Unit = { }
        val command = ScheduleCommand.Events.rescheduleAfter(withDelay = false, block = block) as ScheduleCommand.EventsCommand
        assertThat(command.action).isEqualTo(ScheduleCommand.Action.RESCHEDULE)
        assertThat(command.withDelay).isFalse()
        assertThat(command.blockWhileUnscheduled).isSameInstanceAs(block)
    }

    @Test
    fun `images commands build expected command`() {
        assertThat(ScheduleCommand.Images.reschedule())
            .isEqualTo(ScheduleCommand.ImagesCommand(action = ScheduleCommand.Action.RESCHEDULE))

        assertThat(ScheduleCommand.Images.unschedule())
            .isEqualTo(ScheduleCommand.ImagesCommand(action = ScheduleCommand.Action.UNSCHEDULE))

        val block: suspend () -> Unit = { }
        val command = ScheduleCommand.Images.rescheduleAfter(block) as ScheduleCommand.ImagesCommand
        assertThat(command.action).isEqualTo(ScheduleCommand.Action.RESCHEDULE)
        assertThat(command.blockWhileUnscheduled).isSameInstanceAs(block)
    }
}
