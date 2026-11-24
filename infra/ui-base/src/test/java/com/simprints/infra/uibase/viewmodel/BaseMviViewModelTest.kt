package com.simprints.infra.uibase.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import app.cash.turbine.test
import com.google.common.truth.Truth.*
import com.simprints.infra.logging.Simber
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Rule
import org.junit.Test
import kotlin.coroutines.cancellation.CancellationException

class BaseMviViewModelTest {
    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private val store = ViewModelStore()

    @After
    fun tearDown() {
        store.clear()
    }

    @Test
    fun `Initial state is exposed before any action`() {
        val viewModel = createViewModel(initialState = 42)

        assertThat(viewModel.uiState.value).isEqualTo(42)
        assertThat(viewModel.handled).isEmpty()
    }

    @Test
    fun `Reducers applied in one action are reflected in order`() {
        val viewModel = createViewModel(initialState = 3)

        viewModel.onAction(TestAction.AddOneThenDouble)

        // (3 + 1) * 2 = 8; the reverse order would give 3 * 2 + 1 = 7
        assertThat(viewModel.uiState.value).isEqualTo(8)
    }

    @Test
    fun `Second action waits until the suspended first action completes`() = runTest {
        val viewModel = createViewModel(initialState = 0)
        val gate = CompletableDeferred<Unit>()
        val actionA = TestAction.AwaitThenAdd(gate, amount = 10)
        val actionB = TestAction.ObserveState

        viewModel.onAction(actionA)
        viewModel.onAction(actionB)

        assertThat(viewModel.handled).containsExactly(actionA)
        assertThat(viewModel.observedStates).isEmpty()

        gate.complete(Unit)

        assertThat(viewModel.handled).containsExactly(actionA, actionB).inOrder()
        assertThat(viewModel.observedStates).containsExactly(10)
    }

    @Test
    fun `Effect emitted before anything collects is delivered to a later collector`() = runTest {
        val viewModel = createViewModel(initialState = 0)

        viewModel.onAction(TestAction.Emit("E"))

        viewModel.uiEffect.test {
            assertThat(awaitItem()).isEqualTo("E")
            expectNoEvents()
        }
    }

    @Test
    fun `Effect delivered to a cancelled collector is not replayed to the next one`() = runTest {
        val viewModel = createViewModel(initialState = 0)
        viewModel.onAction(TestAction.Emit("E"))

        viewModel.uiEffect.test {
            assertThat(awaitItem()).isEqualTo("E")
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.uiEffect.test {
            expectNoEvents()
        }
    }

    @Test
    fun `Effects emitted with no active collector are delivered in order and once to the next collector`() = runTest {
        val viewModel = createViewModel(initialState = 0)
        viewModel.uiEffect.test {
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.onAction(TestAction.Emit("E1"))
        viewModel.onAction(TestAction.Emit("E2"))

        viewModel.uiEffect.test {
            assertThat(awaitItem()).isEqualTo("E1")
            assertThat(awaitItem()).isEqualTo("E2")
            expectNoEvents()
        }
        viewModel.uiEffect.test {
            expectNoEvents()
        }
    }

    @Test
    fun `Work started with launchWork does not block the action queue`() = runTest {
        val viewModel = createViewModel(initialState = 0)
        val gate = CompletableDeferred<Unit>()
        val actionA = TestAction.StartWork(gate)
        val actionB = TestAction.ObserveState

        viewModel.onAction(actionA)
        viewModel.onAction(actionB)

        assertThat(viewModel.workJob?.isActive).isTrue()
        assertThat(viewModel.handled).containsExactly(actionA, actionB).inOrder()
        assertThat(viewModel.observedStates).containsExactly(0)

        gate.complete(Unit)

        assertThat(viewModel.workJob?.isCompleted).isTrue()
        assertThat(viewModel.uiState.value).isEqualTo(1)
    }

    @Test
    fun `CancellationException from an action while active keeps the queue running`() = runTest {
        val viewModel = createViewModel(initialState = 0)
        val actionA = TestAction.AddThenThrowCancellation(amount = 5)
        val actionB = TestAction.ObserveState

        viewModel.onAction(actionA)
        viewModel.onAction(actionB)

        assertThat(viewModel.handled).containsExactly(actionA, actionB).inOrder()
        assertThat(viewModel.uiState.value).isEqualTo(5)
        assertThat(viewModel.observedStates).containsExactly(5)
    }

    @Test
    fun `CancellationException from an action while active is logged with the dropped action`() = runTest {
        // mockkStatic, not mockkObject: Simber.i is @JvmStatic, which mockkObject does not intercept
        mockkStatic(Simber::class)
        try {
            val viewModel = createViewModel(initialState = 0)

            viewModel.onAction(TestAction.AddThenThrowCancellation(amount = 5))

            verify(exactly = 1) {
                Simber.i(
                    "Dropped AddThenThrowCancellation(amount=5): CancellationException while the ViewModel is active",
                    ofType<CancellationException>(),
                    any<String>(),
                )
            }
        } finally {
            unmockkStatic(Simber::class)
        }
    }

    // Without the switch to the main dispatcher the effect is handed to the waiting collector on the background thread, the collector
    // is cancelled before its resume runs on main, and the effect is lost.
    @Test
    fun `Effect emitted off the main dispatcher is not lost when the screen stops before the main dispatcher runs`() {
        val mainScheduler = TestCoroutineScheduler()
        val main = StandardTestDispatcher(mainScheduler, name = "main")
        Dispatchers.setMain(main) // reset by TestCoroutineRule
        val backgroundScheduler = TestCoroutineScheduler()
        val background = StandardTestDispatcher(backgroundScheduler, name = "background")
        val scope = CoroutineScope(Job())
        val viewModel = createViewModel(initialState = 0)
        val received = mutableListOf<String>()

        try {
            val stoppedScreen = scope.launch(main) { viewModel.uiEffect.collect { received += "stopped screen: $it" } }
            mainScheduler.runCurrent() // the screen waits for an effect

            scope.launch(background) { viewModel.emitForTest("E") }
            backgroundScheduler.runCurrent()
            stoppedScreen.cancel() // the screen stops before the main dispatcher runs again
            mainScheduler.runCurrent()

            scope.launch(main) { viewModel.uiEffect.collect { received += "restarted screen: $it" } }
            mainScheduler.runCurrent()

            assertThat(received).containsExactly("restarted screen: E")
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun `After the store is cleared actions are not handled and running work is cancelled`() = runTest {
        val viewModel = createViewModel(initialState = 0)
        val gate = CompletableDeferred<Unit>()
        val startWork = TestAction.StartWork(gate)
        viewModel.onAction(startWork)
        assertThat(viewModel.workJob?.isActive).isTrue()

        store.clear()
        viewModel.onAction(TestAction.ObserveState)
        gate.complete(Unit)

        assertThat(viewModel.workJob?.isCancelled).isTrue()
        assertThat(viewModel.handled).containsExactly(startWork)
        assertThat(viewModel.observedStates).isEmpty()
        assertThat(viewModel.uiState.value).isEqualTo(0)
    }

    @Test
    fun `Clearing the store while an action is suspended stops the queue and drops queued actions`() = runTest {
        val viewModel = createViewModel(initialState = 0)
        val actionA = TestAction.AwaitThenAdd(CompletableDeferred(), amount = 10)
        viewModel.onAction(actionA)
        viewModel.onAction(TestAction.ObserveState)

        // Cancellation reaches the loop inside handleAction, so the §3.3 guard must rethrow it
        store.clear()

        assertThat(viewModel.handled).containsExactly(actionA)
        assertThat(viewModel.observedStates).isEmpty()
        assertThat(viewModel.uiState.value).isEqualTo(0)
    }

    @Test
    fun `onAction called inside handleAction is queued after the current action, not handled recursively`() = runTest {
        val viewModel = createViewModel(initialState = 3)
        val actionB = TestAction.AddOneThenDouble
        val actionA = TestAction.SendFromHandler(next = actionB)

        viewModel.onAction(actionA)

        assertThat(viewModel.trace)
            .containsExactly("start $actionA", "end $actionA", "start $actionB", "end $actionB")
            .inOrder()
        assertThat(viewModel.uiState.value).isEqualTo(8)
    }

    private fun createViewModel(initialState: Int): TestMviViewModel {
        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = TestMviViewModel(initialState) as T
        }
        return ViewModelProvider(store, factory)[TestMviViewModel::class.java]
    }

    private sealed interface TestAction {
        data object AddOneThenDouble : TestAction

        data class AwaitThenAdd(
            val gate: CompletableDeferred<Unit>,
            val amount: Int,
        ) : TestAction

        data object ObserveState : TestAction

        data class Emit(
            val effect: String,
        ) : TestAction

        data class StartWork(
            val gate: CompletableDeferred<Unit>,
        ) : TestAction

        data class AddThenThrowCancellation(
            val amount: Int,
        ) : TestAction

        data class SendFromHandler(
            val next: TestAction,
        ) : TestAction
    }

    private class TestMviViewModel(
        initialState: Int,
    ) : BaseMviViewModel<Int, TestAction, String>(initialState) {
        val handled = mutableListOf<TestAction>()
        val observedStates = mutableListOf<Int>()
        val trace = mutableListOf<String>()
        var workJob: Job? = null

        suspend fun emitForTest(effect: String) = emitEffect(effect)

        override suspend fun handleAction(action: TestAction) {
            handled += action
            trace += "start $action"
            when (action) {
                TestAction.AddOneThenDouble -> {
                    reduce { it + 1 }
                    reduce { it * 2 }
                }

                is TestAction.AwaitThenAdd -> {
                    action.gate.await()
                    reduce { it + action.amount }
                }

                TestAction.ObserveState -> observedStates += uiState.value

                is TestAction.Emit -> emitEffect(action.effect)

                is TestAction.StartWork -> workJob = launchWork {
                    action.gate.await()
                    reduce { it + 1 }
                }

                is TestAction.AddThenThrowCancellation -> {
                    reduce { it + action.amount }
                    throw CancellationException("Collaborator cancelled while the ViewModel is active")
                }

                is TestAction.SendFromHandler -> onAction(action.next)
            }
            trace += "end $action"
        }
    }
}
