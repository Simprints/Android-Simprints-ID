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

    internal data class UpSyncCommand(
        val action: Action,
        val withDelay: Boolean = false,
    ) : ScheduleCommand()

    internal data class DownSyncCommand(
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

    object UpSync {
        fun reschedule(withDelay: Boolean = false): ScheduleCommand = UpSyncCommand(action = Action.RESCHEDULE, withDelay = withDelay)

        fun unschedule(): ScheduleCommand = UpSyncCommand(action = Action.UNSCHEDULE)
    }

    object DownSync {
        fun reschedule(withDelay: Boolean = false): ScheduleCommand = DownSyncCommand(action = Action.RESCHEDULE, withDelay = withDelay)

        fun unschedule(): ScheduleCommand = DownSyncCommand(action = Action.UNSCHEDULE)
    }

    object Images {
        fun reschedule(): ScheduleCommand = ImagesCommand(action = Action.RESCHEDULE)

        fun unschedule(): ScheduleCommand = ImagesCommand(action = Action.UNSCHEDULE)
    }

    @Deprecated(
        "Use ScheduleCommand.UpSync and ScheduleCommand.DownSync separately",
        ReplaceWith("ScheduleCommand.UpSync or ScheduleCommand.DownSync"),
    )
    object Events {
        fun reschedule(withDelay: Boolean = false): ScheduleCommand = EverythingCommand(action = Action.RESCHEDULE, withDelay = withDelay)

        fun unschedule(): ScheduleCommand = EverythingCommand(action = Action.UNSCHEDULE)
    }
}
