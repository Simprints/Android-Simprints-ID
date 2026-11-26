package com.simprints.feature.dashboard.tools

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.cancel
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * Cancels the viewModelScope of every tracked ViewModel when the test ends, as clearing the ViewModel would.
 * Without it, work started in init or by launchWork outlives the test and can touch the next test's mocks.
 *
 * Declare it inside TestCoroutineRule, so the scopes are cancelled while the test Main dispatcher is still set:
 * `@get:Rule(order = 0) val testCoroutineRule = TestCoroutineRule()` and `@get:Rule(order = 1) val viewModels = ViewModelScopeRule()`.
 *
 * Public: JUnit rule fields must be public, and a public field cannot expose an internal type.
 */
class ViewModelScopeRule : TestRule {
    private val tracked = mutableListOf<ViewModel>()

    fun <T : ViewModel> track(viewModel: T): T = viewModel.also { tracked += it }

    override fun apply(
        base: Statement,
        description: Description,
    ) = object : Statement() {
        override fun evaluate() {
            try {
                base.evaluate()
            } finally {
                tracked.forEach { it.viewModelScope.cancel() }
                tracked.clear()
            }
        }
    }
}
