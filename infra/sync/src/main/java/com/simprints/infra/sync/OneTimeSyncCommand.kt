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

    internal data class UpSyncCommand(
        val action: Action,
    ) : OneTime()

    internal data class DownSyncCommand(
        val action: Action,
    ) : OneTime()

    internal data class ImagesCommand(
        val action: Action,
    ) : OneTime()

    object UpSync {
        fun start(): OneTime = UpSyncCommand(Action.START)

        fun stop(): OneTime = UpSyncCommand(Action.STOP)

        fun restart(): OneTime = UpSyncCommand(Action.RESTART)
    }

    object DownSync {
        fun start(): OneTime = DownSyncCommand(Action.START)

        fun stop(): OneTime = DownSyncCommand(Action.STOP)

        fun restart(): OneTime = DownSyncCommand(Action.RESTART)
    }

    object Images {
        fun start(): OneTime = ImagesCommand(Action.START)

        fun stop(): OneTime = ImagesCommand(Action.STOP)

        fun restart(): OneTime = ImagesCommand(Action.RESTART)
    }

    @Deprecated(
        "Use OneTime.UpSync and OneTime.DownSync separately",
        ReplaceWith("OneTime.UpSync or OneTime.DownSync"),
    )
    object Events {
        fun start(isDownSyncAllowed: Boolean = true): OneTime = UpSyncCommand(Action.START)

        fun stop(): OneTime = UpSyncCommand(Action.STOP)

        fun restart(isDownSyncAllowed: Boolean = true): OneTime = UpSyncCommand(Action.RESTART)
    }
}
