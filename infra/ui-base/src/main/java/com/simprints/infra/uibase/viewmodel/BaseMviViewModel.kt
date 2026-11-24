package com.simprints.infra.uibase.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.infra.logging.Simber
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException

/**
 * Shared base class for every MVI screen ViewModel.
 *
 * - State: a single immutable [UiState], updated only through [reduce].
 * - Actions: every UI and system input goes through [onAction]; actions are handled one at a time, in send order.
 * - Effects: one-time effects go through [emitEffect]; they are buffered while nobody collects and delivered once.
 */
abstract class BaseMviViewModel<UiState : Any, UiAction : Any, UiEffect : Any>(
    initialState: UiState,
) : ViewModel() {
    private val _uiState = MutableStateFlow(initialState)

    /**
     * Current screen state. Open so stream-composed ViewModels can expose a derived StateFlow.
     */
    open val uiState: StateFlow<UiState> get() = _uiState

    private val effects = Channel<UiEffect>(Channel.BUFFERED)

    /**
     * One-time effects. Single collector; buffered while nobody collects; each effect delivered once.
     * Effects sent while the UI is stopped are delivered when it starts again.
     */
    val uiEffect: Flow<UiEffect> = effects.receiveAsFlow()

    // Actions are handled sequentially, in the order they were sent.
    private val actions = Channel<UiAction>(Channel.UNLIMITED)

    init {
        viewModelScope.launch {
            for (action in actions) {
                try {
                    handleAction(action)
                } catch (e: CancellationException) {
                    // The ViewModel is being cleared: stop the loop.
                    if (!currentCoroutineContext().isActive) throw e
                    // Otherwise a collaborator threw CancellationException while the scope is alive:
                    // drop this action and keep the queue running, like a failed `viewModelScope.launch` did before MVI.
                    // toString(), not the class name: R8 renames classes, while data class/object toString() keeps the name.
                    // Actions must therefore not carry sensitive data.
                    Simber.i("Dropped $action: CancellationException while the ViewModel is active", e)
                }
            }
        }
    }

    /**
     * Handles one action. Called sequentially, in send order, on viewModelScope.
     */
    protected abstract suspend fun handleAction(action: UiAction)

    /**
     * Entry point from the UI. Never suspends, never throws, never blocks.
     */
    fun onAction(action: UiAction) {
        actions.trySend(action)
    }

    protected fun reduce(reducer: (UiState) -> UiState) = _uiState.update(reducer)

    /**
     * Sends a one-time effect. Safe from any dispatcher: the send always happens on the main dispatcher, where the screen collects.
     * A send from another thread would hand the effect to the waiting collector and only then resume it on the main thread;
     * if the screen stopped in between, the effect would be lost.
     */
    protected suspend fun emitEffect(effect: UiEffect) {
        withContext(Dispatchers.Main.immediate) { effects.send(effect) }
    }

    protected fun launchWork(block: suspend CoroutineScope.() -> Unit): Job = viewModelScope.launch(block = block)
}
