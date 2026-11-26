package com.simprints.feature.dashboard.tools

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.common.truth.Truth.*
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import org.junit.Rule
import org.junit.Test
import org.junit.runner.Description
import org.junit.runners.model.Statement

class ViewModelScopeRuleTest {
    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @Test
    fun `work of a tracked ViewModel is cancelled when the test ends`() {
        val rule = ViewModelScopeRule()
        val tracked = object : ViewModel() {}
        val untracked = object : ViewModel() {}
        lateinit var trackedWork: Job
        lateinit var untrackedWork: Job

        rule.evaluate {
            trackedWork = rule.track(tracked).viewModelScope.launch { awaitCancellation() }
            untrackedWork = untracked.viewModelScope.launch { awaitCancellation() }
            assertThat(trackedWork.isActive).isTrue()
        }

        assertThat(trackedWork.isCancelled).isTrue()
        assertThat(untrackedWork.isActive).isTrue()
        untrackedWork.cancel()
    }

    @Test
    fun `work is cancelled when the test fails`() {
        val rule = ViewModelScopeRule()
        lateinit var work: Job

        runCatching {
            rule.evaluate {
                work = rule.track(object : ViewModel() {}).viewModelScope.launch { awaitCancellation() }
                error("test failure")
            }
        }

        assertThat(work.isCancelled).isTrue()
    }

    private fun ViewModelScopeRule.evaluate(body: () -> Unit) = apply(
        object : Statement() {
            override fun evaluate() = body()
        },
        Description.EMPTY,
    ).evaluate()
}
