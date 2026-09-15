package com.simprints.feature.moduleselector.arch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

internal abstract class MviViewModel<A : UiAction, S : State, M : UiModel, E : UiEffect>(
    initialDataState: S,
    private val mapper: StateToModelMapper<S, M>,
) : ViewModel() {
    val uiModel: StateFlow<M>
        field = MutableStateFlow(mapper.mapStateToModel(initialDataState))
    val effects: SharedFlow<E>
        field = MutableSharedFlow<E>(extraBufferCapacity = 1)

    protected var state: S = initialDataState
        private set

    private val changeQueue = Channel<(S) -> S>(Channel.UNLIMITED)
    private val actionQueue = Channel<A>(Channel.UNLIMITED)

    init {
        viewModelScope.launch {
            changeQueue.consumeEach { transform ->
                state = transform(state)
                uiModel.value = mapper.mapStateToModel(state)
            }
        }
        viewModelScope.launch {
            actionQueue.consumeEach { action -> processAction(action) }
        }
    }

    fun onAction(action: A) {
        viewModelScope.launch { actionQueue.send(action) }
    }

    protected abstract suspend fun processAction(action: A)

    protected fun updateState(transform: (S) -> S) {
        viewModelScope.launch { changeQueue.send(transform) }
    }

    protected fun sendEffect(effect: E) {
        effects.tryEmit(effect)
    }
}
