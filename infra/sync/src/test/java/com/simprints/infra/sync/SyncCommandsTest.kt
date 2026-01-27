package com.simprints.infra.sync

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SyncCommandsTest {
    private val buildersWithoutParams = listOf(
        SyncCommands.OneTime.Images to SyncTarget.ONE_TIME_IMAGES,
        SyncCommands.Schedule.Images to SyncTarget.SCHEDULE_IMAGES,
    )

    private val buildersWithDelayParam = listOf(
        SyncCommands.Schedule.Everything to SyncTarget.SCHEDULE_EVERYTHING,
        SyncCommands.Schedule.Events to SyncTarget.SCHEDULE_EVENTS,
    )

    private val buildersWithDownSyncAllowedParam = listOf(
        SyncCommands.OneTime.Events to SyncTarget.ONE_TIME_EVENTS,
    )

    @Test
    fun `stop builds executable command without params`() {
        buildersWithoutParams.forEach { (builder, expectedTarget) ->
            assertThat(builder.stop())
                .isEqualTo(expectedCommand(expectedTarget, SyncAction.STOP))
        }
        buildersWithDownSyncAllowedParam.forEach { (builder, expectedTarget) ->
            assertThat(builder.stop())
                .isEqualTo(expectedCommand(expectedTarget, SyncAction.STOP))
        }
        buildersWithDelayParam.forEach { (builder, expectedTarget) ->
            assertThat(builder.stop())
                .isEqualTo(expectedCommand(expectedTarget, SyncAction.STOP))
        }
    }

    @Test
    fun `start builds executable command with expected params`() {
        buildersWithoutParams.forEach { (builder, expectedTarget) ->
            assertThat(builder.start())
                .isEqualTo(expectedCommand(expectedTarget, SyncAction.START))
        }

        buildersWithDownSyncAllowedParam.forEach { (builder, expectedTarget) ->
            assertThat(builder.start())
                .isEqualTo(
                    expectedCommand(
                        target = expectedTarget,
                        action = SyncAction.START,
                        payload = SyncCommandPayload.WithDownSyncAllowed(true),
                    ),
                )
            assertThat(builder.start(isDownSyncAllowed = false))
                .isEqualTo(
                    expectedCommand(
                        target = expectedTarget,
                        action = SyncAction.START,
                        payload = SyncCommandPayload.WithDownSyncAllowed(false),
                    ),
                )
        }

        buildersWithDelayParam.forEach { (builder, expectedTarget) ->
            assertThat(builder.start())
                .isEqualTo(
                    expectedCommand(
                        target = expectedTarget,
                        action = SyncAction.START,
                        payload = SyncCommandPayload.WithDelay(false),
                    ),
                )
            assertThat(builder.start(withDelay = true))
                .isEqualTo(
                    expectedCommand(
                        target = expectedTarget,
                        action = SyncAction.START,
                        payload = SyncCommandPayload.WithDelay(true),
                    ),
                )
        }
    }

    @Test
    fun `stopAndStart builds executable command with expected params`() {
        val action = SyncAction.STOP_AND_START
        buildersWithoutParams.forEach { (builder, expectedTarget) ->
            assertThat(builder.stopAndStart())
                .isEqualTo(expectedCommand(expectedTarget, action))
        }

        buildersWithDelayParam.forEach { (builder, expectedTarget) ->
            assertThat(builder.stopAndStart())
                .isEqualTo(
                    expectedCommand(
                        target = expectedTarget,
                        action,
                        payload = SyncCommandPayload.WithDelay(false),
                    ),
                )
            assertThat(builder.stopAndStart(withDelay = true))
                .isEqualTo(
                    expectedCommand(
                        target = expectedTarget,
                        action,
                        payload = SyncCommandPayload.WithDelay(true),
                    ),
                )
        }

        buildersWithDownSyncAllowedParam.forEach { (builder, expectedTarget) ->
            assertThat(builder.stopAndStart())
                .isEqualTo(
                    expectedCommand(
                        target = expectedTarget,
                        action,
                        payload = SyncCommandPayload.WithDownSyncAllowed(true),
                    ),
                )
            assertThat(builder.stopAndStart(isDownSyncAllowed = false))
                .isEqualTo(
                    expectedCommand(
                        target = expectedTarget,
                        action,
                        payload = SyncCommandPayload.WithDownSyncAllowed(false),
                    ),
                )
        }
    }

    @Test
    fun `stopAndStartAround builds executable command and stores block`() {
        val block: suspend () -> Unit = { }
        val action = SyncAction.STOP_AND_START

        buildersWithoutParams.forEach { (builder, expectedTarget) ->
            assertThat(builder.stopAndStartAround(block))
                .isEqualTo(
                    expectedCommand(
                        target = expectedTarget,
                        action = action,
                        block = block,
                    ),
                )
        }

        buildersWithDownSyncAllowedParam.forEach { (builder, expectedTarget) ->
            assertThat(builder.stopAndStartAround(block = block))
                .isEqualTo(
                    expectedCommand(
                        target = expectedTarget,
                        action,
                        payload = SyncCommandPayload.WithDownSyncAllowed(true),
                        block,
                    ),
                )
            assertThat(builder.stopAndStartAround(isDownSyncAllowed = false, block = block))
                .isEqualTo(
                    expectedCommand(
                        target = expectedTarget,
                        action,
                        payload = SyncCommandPayload.WithDownSyncAllowed(false),
                        block,
                    ),
                )
        }

        buildersWithDelayParam.forEach { (builder, expectedTarget) ->
            assertThat(builder.stopAndStartAround(block = block))
                .isEqualTo(
                    expectedCommand(
                        target = expectedTarget,
                        action,
                        payload = SyncCommandPayload.WithDelay(false),
                        block,
                    ),
                )
            assertThat(builder.stopAndStartAround(withDelay = true, block = block))
                .isEqualTo(
                    expectedCommand(
                        target = expectedTarget,
                        action,
                        payload = SyncCommandPayload.WithDelay(true),
                        block,
                    ),
                )
        }
    }

    @Test
    fun `observe only is not an executable command`() {
        assertThat(ExecutableSyncCommand::class.java.isInstance(SyncCommands.ObserveOnly))
            .isFalse()
    }

    private fun expectedCommand(
        target: SyncTarget,
        action: SyncAction,
        payload: SyncCommandPayload = SyncCommandPayload.None,
        block: (suspend () -> Unit)? = null,
    ) = ExecutableSyncCommand(
        target = target,
        action = action,
        payload = payload,
        blockToRunWhileStopped = block,
    )
}
