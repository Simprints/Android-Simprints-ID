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

    object OneTime {
        val Events = buildSyncCommandsWithDownSyncParam(SyncTarget.ONE_TIME_EVENTS)
        val Images = buildSyncCommands(SyncTarget.ONE_TIME_IMAGES)
    }

    object Schedule {
        val Everything = buildSyncCommandsWithDelayParam(SyncTarget.SCHEDULE_EVERYTHING)
        val Events = buildSyncCommandsWithDelayParam(SyncTarget.SCHEDULE_EVENTS)
        val Images = buildSyncCommands(SyncTarget.SCHEDULE_IMAGES)
    }

    // builders

    interface SyncCommandBuilder {
        fun stop(): SyncCommand

        fun start(): SyncCommand

        fun stopAndStart(): SyncCommand

        fun stopAndStartAround(block: suspend () -> Unit): SyncCommand
    }

    interface SyncCommandBuilderWithDownSyncParam {
        fun stop(): SyncCommand

        fun start(isDownSyncAllowed: Boolean = true): SyncCommand

        fun stopAndStart(isDownSyncAllowed: Boolean = true): SyncCommand

        fun stopAndStartAround(
            isDownSyncAllowed: Boolean = true,
            block: suspend () -> Unit,
        ): SyncCommand
    }

    interface SyncCommandBuilderWithDelayParam {
        fun stop(): SyncCommand

        fun start(withDelay: Boolean = false): SyncCommand

        fun stopAndStart(withDelay: Boolean = false): SyncCommand

        fun stopAndStartAround(
            withDelay: Boolean = false,
            block: suspend () -> Unit,
        ): SyncCommand
    }

    private fun buildSyncCommands(target: SyncTarget): SyncCommandBuilder = object : SyncCommandBuilder {
        override fun stop() = getCommand(target, SyncAction.STOP)

        override fun start() = getCommand(target, SyncAction.START)

        override fun stopAndStart() = getCommand(target, SyncAction.STOP_AND_START)

        override fun stopAndStartAround(block: suspend () -> Unit) = getCommand(target, SyncAction.STOP_AND_START, null, block)
    }

    private fun buildSyncCommandsWithDownSyncParam(target: SyncTarget) = object : SyncCommandBuilderWithDownSyncParam {
        override fun stop() = getCommand(target, SyncAction.STOP)

        override fun start(isDownSyncAllowed: Boolean) =
            getCommand(target, SyncAction.START, SyncParam.IS_DOWN_SYNC_ALLOWED to isDownSyncAllowed)

        override fun stopAndStart(isDownSyncAllowed: Boolean) =
            getCommand(target, SyncAction.STOP_AND_START, SyncParam.IS_DOWN_SYNC_ALLOWED to isDownSyncAllowed)

        override fun stopAndStartAround(
            isDownSyncAllowed: Boolean,
            block: suspend () -> Unit,
        ) = getCommand(target, SyncAction.STOP_AND_START, SyncParam.IS_DOWN_SYNC_ALLOWED to isDownSyncAllowed, block)
    }

    private fun buildSyncCommandsWithDelayParam(target: SyncTarget) = object : SyncCommandBuilderWithDelayParam {
        override fun stop() = getCommand(target, SyncAction.STOP)

        override fun start(withDelay: Boolean) = getCommand(target, SyncAction.START, SyncParam.WITH_DELAY to withDelay)

        override fun stopAndStart(withDelay: Boolean) = getCommand(target, SyncAction.STOP_AND_START, SyncParam.WITH_DELAY to withDelay)

        override fun stopAndStartAround(
            withDelay: Boolean,
            block: suspend () -> Unit,
        ) = getCommand(target, SyncAction.STOP_AND_START, SyncParam.WITH_DELAY to withDelay, block)
    }

    private fun getCommand(
        target: SyncTarget,
        action: SyncAction,
        param: Pair<SyncParam, Any>? = null,
        block: (suspend () -> Unit)? = null,
    ) = ExecutableSyncCommand(
        target,
        action,
        param?.run { mapOf(first to second) } ?: emptyMap(),
        block,
    )
}

/**
 * Complete command built from SyncCommands and bundled with instructions ready to be processed by SyncUseCase.
 */
sealed class SyncCommand

internal data class ExecutableSyncCommand(
    val target: SyncTarget,
    val action: SyncAction,
    val params: Map<SyncParam, Any> = emptyMap(),
    val blockToRunWhileStopped: (suspend () -> Unit)? = null,
) : SyncCommand()

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
    STOP_AND_START,
}

internal enum class SyncParam {
    IS_DOWN_SYNC_ALLOWED,
    WITH_DELAY,
}
