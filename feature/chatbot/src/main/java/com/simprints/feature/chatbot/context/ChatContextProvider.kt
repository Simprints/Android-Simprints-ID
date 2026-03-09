package com.simprints.feature.chatbot.context

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import com.simprints.infra.aichat.model.ChatContext
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.network.ConnectivityTracker
import com.simprints.logging.persistent.LogEntryType
import com.simprints.logging.persistent.PersistentLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Provides real-time context about the user's current app state to enrich
 * chatbot prompts. Collects screen name, project config, device diagnostics,
 * recent logs and errors.
 */
@Singleton
internal class ChatContextProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val configRepository: ConfigRepository,
    private val connectivityTracker: ConnectivityTracker,
    private val persistentLogger: PersistentLogger,
) {
    private val _currentScreen = MutableStateFlow("")
    private val _currentStep = MutableStateFlow("")
    private val _totalSteps = MutableStateFlow(0)
    private val _currentStepIndex = MutableStateFlow(0)
    private val _workflowType = MutableStateFlow("")

    fun updateScreen(screenName: String) {
        _currentScreen.value = screenName
    }

    fun updateStep(stepName: String, index: Int, total: Int) {
        _currentStep.value = stepName
        _currentStepIndex.value = index
        _totalSteps.value = total
    }

    fun updateWorkflow(workflowType: String) {
        _workflowType.value = workflowType
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
            workflowType = _workflowType.value,
            projectName = project?.name ?: "",
            enabledModalities = modalities,
            scannerType = scanners,
            isConnected = connectivityTracker.isConnected(),
            recentErrors = collectRecentErrors(),
            recentLogs = collectRecentLogs(),
            appVersion = getAppVersion(),
            androidVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
            freeStorageMb = getFreeStorageMb(),
            batteryPercent = getBatteryPercent(),
        )
    }

    private suspend fun collectRecentLogs(): List<String> = runCatching {
        val intents = persistentLogger.get(LogEntryType.Intent)
        val network = persistentLogger.get(LogEntryType.Network)
        (intents + network)
            .sortedByDescending { it.timestampMs }
            .take(MAX_RECENT_LOGS)
            .map { "${it.type.name}: ${it.title} — ${it.body}" }
    }.getOrDefault(emptyList())

    private suspend fun collectRecentErrors(): List<String> = runCatching {
        val network = persistentLogger.get(LogEntryType.Network)
        network
            .filter { it.title.contains("error", ignoreCase = true) || it.title.contains("fail", ignoreCase = true) }
            .sortedByDescending { it.timestampMs }
            .take(MAX_RECENT_ERRORS)
            .map { "${it.title}: ${it.body}" }
    }.getOrDefault(emptyList())

    private fun getAppVersion(): String = runCatching {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: ""
    }.getOrDefault("")

    private fun getFreeStorageMb(): Long = runCatching {
        val stat = StatFs(Environment.getDataDirectory().path)
        stat.availableBlocksLong * stat.blockSizeLong / (1024 * 1024)
    }.getOrDefault(-1L)

    private fun getBatteryPercent(): Int = runCatching {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        if (level >= 0 && scale > 0) (level * 100) / scale else -1
    }.getOrDefault(-1)

    companion object {
        private const val MAX_RECENT_LOGS = 10
        private const val MAX_RECENT_ERRORS = 5
    }
}
