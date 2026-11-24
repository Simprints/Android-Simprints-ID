package com.simprints.feature.dashboard.tools

import com.simprints.infra.uibase.viewmodel.BaseMviViewModel
import io.mockk.every
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow

internal class MviStubs<S : Any, E : Any>(
    initial: S,
) {
    val state = MutableStateFlow(initial)
    val effects = Channel<E>(Channel.BUFFERED)
}

/** Call on a MockK mock of a BaseMviViewModel subclass. Stubs uiState and uiEffect; onAction stays a recorded no-op. */
internal fun <S : Any, A : Any, E : Any> BaseMviViewModel<S, A, E>.stubMvi(initial: S): MviStubs<S, E> =
    MviStubs<S, E>(initial).also { stubs ->
        every { this@stubMvi.uiState } returns stubs.state
        every { this@stubMvi.uiEffect } returns stubs.effects.receiveAsFlow()
    }
