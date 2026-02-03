package com.simprints.infra.sync

/**
 * Periodic/background scheduling commands.
 *
 * Intended to be executed via [SyncOrchestrator.execute].
 */
sealed class ScheduleCommand {
    internal enum class Action {
        RESCHEDULE,
        UNSCHEDULE,
    }

    internal data class EverythingCommand(
        val action: Action,
        val withDelay: Boolean = false,
        val blockWhileUnscheduled: (suspend () -> Unit)? = null,
    ) : ScheduleCommand()

    internal data class EventsCommand(
        val action: Action,
        val withDelay: Boolean = false,
        val blockWhileUnscheduled: (suspend () -> Unit)? = null,
    ) : ScheduleCommand()

    internal data class ImagesCommand(
        val action: Action,
        val blockWhileUnscheduled: (suspend () -> Unit)? = null,
    ) : ScheduleCommand()

    object Everything {
        fun reschedule(withDelay: Boolean = false): ScheduleCommand = EverythingCommand(action = Action.RESCHEDULE, withDelay = withDelay)

        fun unschedule(): ScheduleCommand = EverythingCommand(action = Action.UNSCHEDULE)

        fun rescheduleAfter(
            withDelay: Boolean = false,
            block: suspend () -> Unit,
        ): ScheduleCommand = EverythingCommand(action = Action.RESCHEDULE, withDelay = withDelay, blockWhileUnscheduled = block)
    }

    object Events {
        fun reschedule(withDelay: Boolean = false): ScheduleCommand = EventsCommand(action = Action.RESCHEDULE, withDelay = withDelay)

        fun unschedule(): ScheduleCommand = EventsCommand(action = Action.UNSCHEDULE)

        fun rescheduleAfter(
            withDelay: Boolean = false,
            block: suspend () -> Unit,
        ): ScheduleCommand = EventsCommand(action = Action.RESCHEDULE, withDelay = withDelay, blockWhileUnscheduled = block)
    }

    object Images {
        fun reschedule(): ScheduleCommand = ImagesCommand(action = Action.RESCHEDULE)

        fun unschedule(): ScheduleCommand = ImagesCommand(action = Action.UNSCHEDULE)

        fun rescheduleAfter(block: suspend () -> Unit): ScheduleCommand =
            ImagesCommand(action = Action.RESCHEDULE, blockWhileUnscheduled = block)
    }
}
