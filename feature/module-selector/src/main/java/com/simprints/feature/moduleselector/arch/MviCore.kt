package com.simprints.feature.moduleselector.arch

internal interface UiAction

internal interface State

internal interface UiModel

internal interface UiEffect

internal fun interface StateToModelMapper<S : State, M : UiModel> {
    fun mapStateToModel(state: S): M
}
