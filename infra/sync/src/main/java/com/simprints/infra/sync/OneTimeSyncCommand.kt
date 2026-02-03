package com.simprints.infra.sync

/**
 * One-time (immediate) sync control commands.
 *
 * Intended to be executed via [SyncOrchestrator.execute].
 */
sealed class OneTime {
    internal enum class Action {
        START,
        STOP,
        RESTART,
    }

    internal data class EventsCommand(
        val action: Action,
        val isDownSyncAllowed: Boolean = true,
    ) : OneTime()

    internal data class ImagesCommand(
        val action: Action,
    ) : OneTime()

    object Events {
        fun start(isDownSyncAllowed: Boolean = true): OneTime = EventsCommand(Action.START, isDownSyncAllowed)

        fun stop(): OneTime = EventsCommand(Action.STOP)

        fun restart(isDownSyncAllowed: Boolean = true): OneTime = EventsCommand(Action.RESTART, isDownSyncAllowed)
    }

    object Images {
        fun start(): OneTime = ImagesCommand(Action.START)

        fun stop(): OneTime = ImagesCommand(Action.STOP)

        fun restart(): OneTime = ImagesCommand(Action.RESTART)
    }
}
