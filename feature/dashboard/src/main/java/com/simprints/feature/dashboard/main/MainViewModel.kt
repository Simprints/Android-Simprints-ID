package com.simprints.feature.dashboard.main

import com.simprints.core.DispatcherIO
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.logging.Simber
import com.simprints.infra.security.SecurityManager
import com.simprints.infra.security.exceptions.RootedDeviceException
import com.simprints.infra.uibase.viewmodel.BaseMviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
internal class MainViewModel @Inject constructor(
    private val configRepository: ConfigRepository,
    private val securityManager: SecurityManager,
    @param:DispatcherIO private val ioDispatcher: CoroutineDispatcher,
) : BaseMviViewModel<MainUiState, MainUiAction, MainUiEffect>(MainUiState()) {
    override suspend fun handleAction(action: MainUiAction) {
        when (action) {
            MainUiAction.Load -> load()
        }
    }

    // The root check runs first and does not depend on the configuration. A failing configuration read is logged and leaves the
    // consent requirement unknown (null: the privacy menu item keeps its XML default).
    private suspend fun load() {
        checkIfDeviceIsSafe()
        val isConsentRequired = try {
            configRepository.getProjectConfiguration().consent.collectConsent
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Simber.e("Failed to load project configuration on dashboard", e)
            return
        }
        reduce { it.copy(isConsentRequired = isConsentRequired) }
    }

    private suspend fun checkIfDeviceIsSafe() {
        try {
            withContext(ioDispatcher) { securityManager.checkIfDeviceIsRooted() }
        } catch (ex: RootedDeviceException) {
            Simber.e("Rooted device detected", ex)
            reduce { it.copy(isRootedDeviceDetected = true) }
        }
    }

    init {
        onAction(MainUiAction.Load)
    }
}
