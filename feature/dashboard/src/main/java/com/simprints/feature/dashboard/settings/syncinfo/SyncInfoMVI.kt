package com.simprints.feature.dashboard.settings.syncinfo

import com.simprints.feature.login.LoginParams
import com.simprints.feature.login.LoginResult
import com.simprints.feature.moduleselector.ModuleSelectorResult

internal data class SyncInfoUiState(
    // null until the host sends HostAttached; nothing that depends on the mode runs before it is known. The first value wins.
    val isPreLogoutUpSync: Boolean? = null,
    // The default SyncInfo() is what the screen shows before the first data emission.
    val syncInfo: SyncInfo = SyncInfo(),
)

internal sealed interface SyncInfoUiAction {
    // Sent from onCreateView with the host container's logout-on-complete flag.
    data class HostAttached(
        val isPreLogoutUpSync: Boolean,
    ) : SyncInfoUiAction

    data object SyncRecordsClicked : SyncInfoUiAction

    data object SyncImagesClicked : SyncInfoUiAction

    data object RequestLogin : SyncInfoUiAction

    data class LoginResultReceived(
        val result: LoginResult,
    ) : SyncInfoUiAction

    data class ModuleSelectionResultReceived(
        val result: ModuleSelectorResult,
    ) : SyncInfoUiAction
}

internal sealed interface SyncInfoUiEffect {
    data class NavigateToLogin(
        val params: LoginParams,
    ) : SyncInfoUiEffect
}
