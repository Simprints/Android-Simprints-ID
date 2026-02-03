package com.simprints.infra.sync.extensions

import com.google.common.truth.Truth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class JobExtTest {
    @Test
    fun `await completes when job completes normally`() = runTest {
        val job = Job()
        val awaitDeferred = backgroundScope.async { job.await() }

        runCurrent()
        Truth.assertThat(awaitDeferred.isCompleted).isFalse()

        job.complete()
        runCurrent()
        awaitDeferred.await()
        Truth.assertThat(awaitDeferred.isCompleted).isTrue()
    }

    @Test
    fun `await rethrows failure from job`() = runTest {
        val job = Job()
        val expected = IllegalStateException("ExceptionMessage")

        launch { job.completeExceptionally(expected) }

        val thrown = try {
            job.await()
            null
        } catch (throwable: Throwable) {
            throwable
        }
        Truth.assertThat(thrown).isNotNull()
        Truth.assertThat(thrown).isInstanceOf(IllegalStateException::class.java)
        Truth.assertThat(thrown!!.message).isEqualTo("ExceptionMessage")
        Truth.assertThat(thrown === expected || thrown.cause === expected).isTrue()
    }

    @Test
    fun `await throws CancellationException when job is cancelled`() = runTest {
        val job = Job()
        launch { job.cancel() }

        val thrown = try {
            job.await()
            null
        } catch (throwable: Throwable) {
            throwable
        }
        Truth.assertThat(thrown).isInstanceOf(CancellationException::class.java)
    }
}
