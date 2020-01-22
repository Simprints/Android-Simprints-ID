package com.simprints.id.services.scheduledSync.people.common

import androidx.work.ListenableWorker.Result
import androidx.work.workDataOf
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class WorkerResultSetterImplTest {

    lateinit var setter: WorkerResultSetter
    private val outputData = workDataOf()
    @Before
    fun setup(){
        setter = WorkerResultSetterImpl()
    }

    @Test
    fun successWithOutput_shouldReturnASuccessfulWorkManagerResult() {
        val result = setter.success(outputData)
        assertThat(result).isEqualTo(Result.success(outputData))
    }

    @Test
    fun successWithoutOutput_shouldReturnASuccessfulWorkManagerResult() {
        val result = setter.success()
        assertThat(result).isEqualTo(Result.success())
    }

    @Test
    fun failWithOutput_shouldReturnAFailureWorkManagerResult() {
        val result = setter.failure(outputData)
        assertThat(result).isEqualTo(Result.failure(outputData))
    }

    @Test
    fun failWithoutOutput_shouldReturnAFailureWorkManagerResult() {
        val result = setter.failure()
        assertThat(result).isEqualTo(Result.failure())
    }

    @Test
    fun retryWithoutOutput_shouldReturnARetryWorkManagerResult() {
        val result = setter.retry()
        assertThat(result).isEqualTo(Result.retry())
    }
}
