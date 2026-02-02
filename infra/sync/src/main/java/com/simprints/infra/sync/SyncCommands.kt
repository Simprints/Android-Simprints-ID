package com.simprints.infra.sync

/**
 * Builders for sync control instructions passed to SyncUseCase.
 *
 * To construct a sync command,
 * Start with SyncCommands., and the rest is reachable in a structured way, with appropriate branching and params.
 *
 * See also SyncUseCase.invoke.
 */
object SyncCommands {
    object ObserveOnly : SyncCommand()

    object OneTimeNow {
        // DSL-style capitalization to fit well when used like: sync(SyncCommands.OneTime.Events.start())
        val Events = buildSyncCommandsWithDownSyncParam(SyncTarget.ONE_TIME_EVENTS)
        val Images = buildSyncCommands(SyncTarget.ONE_TIME_IMAGES)
    }

    object ScheduleOf {
        val Everything = buildSyncCommandsWithDelayParam(SyncTarget.SCHEDULE_EVERYTHING)
        val Events = buildSyncCommandsWithDelayParam(SyncTarget.SCHEDULE_EVENTS)
        val Images = buildSyncCommands(SyncTarget.SCHEDULE_IMAGES)
    }

    internal data class ExecutableSyncCommand(
        val target: SyncTarget,
        val action: SyncAction,
        val payload: SyncCommandPayload = SyncCommandPayload.None,
        val blockToRunWhileStopped: (suspend () -> Unit)? = null,
    ) : SyncCommand()

    // builders

    interface SyncCommandBuilder {
        fun stop(): SyncCommand

        fun start(): SyncCommand

        fun restart(): SyncCommand

        fun restartAfter(block: suspend () -> Unit): SyncCommand
    }

    interface SyncCommandBuilderWithDownSyncParam {
        fun stop(): SyncCommand

        fun start(isDownSyncAllowed: Boolean = true): SyncCommand

        fun restart(isDownSyncAllowed: Boolean = true): SyncCommand

        fun restartAfter(
            isDownSyncAllowed: Boolean = true,
            block: suspend () -> Unit,
        ): SyncCommand
    }

    interface SyncCommandBuilderWithDelayParam {
        fun stop(): SyncCommand

        fun start(withDelay: Boolean = false): SyncCommand

        fun restart(withDelay: Boolean = false): SyncCommand

        fun restartAfter(
            withDelay: Boolean = false,
            block: suspend () -> Unit,
        ): SyncCommand
    }

    private fun buildSyncCommands(target: SyncTarget): SyncCommandBuilder = object : SyncCommandBuilder {
        override fun stop() = getCommand(target, SyncAction.STOP)

        override fun start() = getCommand(target, SyncAction.START)

        override fun restart() = getCommand(target, SyncAction.RESTART)

        override fun restartAfter(block: suspend () -> Unit) = getCommand(target, SyncAction.RESTART, block = block)
    }

    private fun buildSyncCommandsWithDownSyncParam(target: SyncTarget) = object : SyncCommandBuilderWithDownSyncParam {
        override fun stop() = getCommand(target, SyncAction.STOP)

        override fun start(isDownSyncAllowed: Boolean) =
            getCommand(target, SyncAction.START, payload = SyncCommandPayload.WithDownSyncAllowed(isDownSyncAllowed))

        override fun restart(isDownSyncAllowed: Boolean) =
            getCommand(target, SyncAction.RESTART, payload = SyncCommandPayload.WithDownSyncAllowed(isDownSyncAllowed))

        override fun restartAfter(
            isDownSyncAllowed: Boolean,
            block: suspend () -> Unit,
        ) = getCommand(
            target,
            SyncAction.RESTART,
            payload = SyncCommandPayload.WithDownSyncAllowed(isDownSyncAllowed),
            block = block,
        )
    }

    private fun buildSyncCommandsWithDelayParam(target: SyncTarget) = object : SyncCommandBuilderWithDelayParam {
        override fun stop() = getCommand(target, SyncAction.STOP)

        override fun start(withDelay: Boolean) = getCommand(target, SyncAction.START, payload = SyncCommandPayload.WithDelay(withDelay))

        override fun restart(withDelay: Boolean) =
            getCommand(target, SyncAction.RESTART, payload = SyncCommandPayload.WithDelay(withDelay))

        override fun restartAfter(
            withDelay: Boolean,
            block: suspend () -> Unit,
        ) = getCommand(target, SyncAction.RESTART, payload = SyncCommandPayload.WithDelay(withDelay), block = block)
    }

    private fun getCommand(
        target: SyncTarget,
        action: SyncAction,
        payload: SyncCommandPayload = SyncCommandPayload.None,
        block: (suspend () -> Unit)? = null,
    ) = ExecutableSyncCommand(
        target,
        action,
        payload,
        block,
    )
}

/**
 * Complete command built from SyncCommands and bundled with instructions ready to be processed by SyncUseCase.
 */
sealed class SyncCommand

enum class SyncTarget {
    SCHEDULE_EVERYTHING,
    ONE_TIME_EVENTS,
    SCHEDULE_EVENTS,
    ONE_TIME_IMAGES,
    SCHEDULE_IMAGES,
}

internal enum class SyncAction {
    STOP,
    START,
    RESTART,
}

internal sealed class SyncCommandPayload {
    object None : SyncCommandPayload()

    data class WithDelay(
        val withDelay: Boolean,
    ) : SyncCommandPayload()

    data class WithDownSyncAllowed(
        val isDownSyncAllowed: Boolean,
    ) : SyncCommandPayload()
}
