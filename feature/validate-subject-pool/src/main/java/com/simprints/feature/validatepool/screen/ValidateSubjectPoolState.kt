package com.simprints.feature.validatepool.screen

internal sealed class ValidateSubjectPoolState {
    data object Validating : ValidateSubjectPoolState()

    data object Success : ValidateSubjectPoolState()

    data object UserMismatch : ValidateSubjectPoolState()

    data object ModuleMismatch : ValidateSubjectPoolState()

    data object RequiresSync : ValidateSubjectPoolState()

    data object SyncInProgress : ValidateSubjectPoolState()

    data object PoolEmpty : ValidateSubjectPoolState()
}
