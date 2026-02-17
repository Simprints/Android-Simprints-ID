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
    ) : ScheduleCommand()

    internal data class EventsCommand(
        val action: Action,
        val withDelay: Boolean = false,
    ) : ScheduleCommand()

    internal data class ImagesCommand(
        val action: Action,
    ) : ScheduleCommand()

    object Everything {
        fun reschedule(withDelay: Boolean = false): ScheduleCommand = EverythingCommand(action = Action.RESCHEDULE, withDelay = withDelay)

        fun unschedule(): ScheduleCommand = EverythingCommand(action = Action.UNSCHEDULE)
    }

    object Events {
        fun reschedule(withDelay: Boolean = false): ScheduleCommand = EventsCommand(action = Action.RESCHEDULE, withDelay = withDelay)

        fun unschedule(): ScheduleCommand = EventsCommand(action = Action.UNSCHEDULE)
    }

    object Images {
        fun reschedule(): ScheduleCommand = ImagesCommand(action = Action.RESCHEDULE)

        fun unschedule(): ScheduleCommand = ImagesCommand(action = Action.UNSCHEDULE)
    }
}
