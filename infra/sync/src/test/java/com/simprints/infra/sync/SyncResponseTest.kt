package com.simprints.infra.sync

import com.google.common.truth.Truth.assertThat
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SyncResponseTest {
    @Test
    fun `await completes when syncCommandJob completes normally`() = runTest {
        val syncCommandJob = Job()
        val response = SyncResponse(
            syncCommandJob = syncCommandJob,
            syncStatusFlow = MutableStateFlow(mockk(relaxed = true)),
        )
        val awaitDeferred = backgroundScope.async { response.await() }

        runCurrent()
        assertThat(awaitDeferred.isCompleted).isFalse()

        syncCommandJob.complete()
        runCurrent()

        awaitDeferred.await()
        assertThat(awaitDeferred.isCompleted).isTrue()
    }

    @Test
    fun `await rethrows failure from syncCommandJob`() = runTest {
        val syncCommandJob = Job()
        val response = SyncResponse(
            syncCommandJob = syncCommandJob,
            syncStatusFlow = MutableStateFlow(mockk(relaxed = true)),
        )
        val expected = IllegalStateException("ExceptionMessage")

        launch { syncCommandJob.completeExceptionally(expected) }

        val thrown = try {
            response.await()
            null
        } catch (throwable: Throwable) {
            throwable
        }
        assertThat(thrown).isNotNull()
        assertThat(thrown).isInstanceOf(IllegalStateException::class.java)
        assertThat(thrown!!.message).isEqualTo("ExceptionMessage")
        assertThat(thrown === expected || thrown.cause === expected).isTrue()
    }

    @Test
    fun `await throws CancellationException when syncCommandJob is cancelled`() = runTest {
        val syncCommandJob = Job()
        val response = SyncResponse(
            syncCommandJob = syncCommandJob,
            syncStatusFlow = MutableStateFlow(mockk(relaxed = true)),
        )

        launch { syncCommandJob.cancel() }

        val thrown = try {
            response.await()
            null
        } catch (throwable: Throwable) {
            throwable
        }
        assertThat(thrown).isInstanceOf(CancellationException::class.java)
    }
}
