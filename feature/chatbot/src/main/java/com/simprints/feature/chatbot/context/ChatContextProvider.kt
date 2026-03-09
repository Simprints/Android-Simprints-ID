package com.simprints.feature.chatbot.context

import com.simprints.infra.aichat.model.ChatContext
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.network.ConnectivityTracker
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Provides real-time context about the user's current app state to enrich
 * chatbot prompts. Collects screen name, project config, recent errors, etc.
 */
@Singleton
internal class ChatContextProvider @Inject constructor(
    private val configRepository: ConfigRepository,
    private val connectivityTracker: ConnectivityTracker,
) {
    private val _currentScreen = MutableStateFlow("")
    private val _currentStep = MutableStateFlow("")
    private val _totalSteps = MutableStateFlow(0)
    private val _currentStepIndex = MutableStateFlow(0)

    fun updateScreen(screenName: String) {
        _currentScreen.value = screenName
    }

    fun updateStep(stepName: String, index: Int, total: Int) {
        _currentStep.value = stepName
        _currentStepIndex.value = index
        _totalSteps.value = total
    }

    suspend fun buildContext(): ChatContext {
        val config = runCatching { configRepository.getProjectConfiguration() }.getOrNull()
        val project = runCatching { configRepository.getProject() }.getOrNull()

        val modalities = config?.general?.modalities?.map { it.name } ?: emptyList()
        val scanners = config?.fingerprint?.allowedScanners
            ?.joinToString(", ") { it.name.replace("_", " ") }
            ?: ""

        return ChatContext(
            currentScreen = _currentScreen.value,
            currentStep = _currentStep.value,
            totalSteps = _totalSteps.value,
            currentStepIndex = _currentStepIndex.value,
            projectName = project?.name ?: "",
            enabledModalities = modalities,
            scannerType = scanners,
            isConnected = connectivityTracker.isConnected(),
            recentErrors = emptyList(),
        )
    }
}
