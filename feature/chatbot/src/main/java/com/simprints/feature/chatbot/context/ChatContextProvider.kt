package com.simprints.feature.chatbot.context

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import com.simprints.infra.aichat.model.ChatContext
import com.simprints.infra.aichat.model.WorkflowStepInfo
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.ConsentConfiguration
import com.simprints.infra.config.store.models.DeviceConfiguration
import com.simprints.infra.config.store.models.FaceConfiguration
import com.simprints.infra.config.store.models.FingerprintConfiguration
import com.simprints.infra.config.store.models.IdentificationConfiguration
import com.simprints.infra.config.store.models.MultiFactorIdConfiguration
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.models.SynchronizationConfiguration
import com.simprints.infra.network.ConnectivityTracker
import com.simprints.logging.persistent.LogEntryType
import com.simprints.logging.persistent.PersistentLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Provides real-time context about the user's current app state to enrich
 * chatbot prompts. Collects workflow state, project config, device diagnostics,
 * recent logs and errors.
 */
@Singleton
class ChatContextProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val configRepository: ConfigRepository,
    private val connectivityTracker: ConnectivityTracker,
    private val persistentLogger: PersistentLogger,
) {
    private val _currentScreen = MutableStateFlow("")
    private val _isInWorkflow = MutableStateFlow(false)
    private val _workflowType = MutableStateFlow("")
    private val _workflowSteps = MutableStateFlow<List<WorkflowStepInfo>>(emptyList())
    private val _requestParameters = MutableStateFlow<Map<String, String>>(emptyMap())
    private val _activeAlert = MutableStateFlow("")

    fun updateScreen(screenName: String) {
        _currentScreen.value = screenName
    }

    fun updateWorkflow(workflowType: String) {
        _isInWorkflow.value = true
        _workflowType.value = workflowType
    }

    fun updateSteps(steps: List<WorkflowStepInfo>) {
        _workflowSteps.value = steps
    }

    fun updateRequestParameters(params: Map<String, String>) {
        _requestParameters.value = params
    }

    fun updateActiveAlert(alert: String) {
        _activeAlert.value = alert
    }

    fun clearActiveAlert() {
        _activeAlert.value = ""
    }

    fun clearWorkflow() {
        _currentScreen.value = ""
        _isInWorkflow.value = false
        _workflowType.value = ""
        _workflowSteps.value = emptyList()
        _requestParameters.value = emptyMap()
        _activeAlert.value = ""
    }

    suspend fun buildContext(): ChatContext {
        val config = runCatching { configRepository.getProjectConfiguration() }.getOrNull()
        val project = runCatching { configRepository.getProject() }.getOrNull()
        val deviceConfig = runCatching { configRepository.getDeviceConfiguration() }.getOrNull()

        return ChatContext(
            currentScreen = _currentScreen.value,
            isInWorkflow = _isInWorkflow.value,
            workflowType = _workflowType.value,
            workflowSteps = _workflowSteps.value,
            requestParameters = _requestParameters.value,
            activeAlert = _activeAlert.value,
            projectName = project?.name ?: "",
            projectConfigSummary = formatConfigSummary(config, deviceConfig),
            isConnected = connectivityTracker.isConnected(),
            recentErrors = collectRecentErrors(),
            recentLogs = collectRecentLogs(),
            appVersion = getAppVersion(),
            androidVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
            freeStorageMb = getFreeStorageMb(),
            batteryPercent = getBatteryPercent(),
        )
    }

    private fun formatConfigSummary(
        config: ProjectConfiguration?,
        deviceConfig: DeviceConfiguration?,
    ): String = buildString {
        config?.let {
            formatGeneral(it)
            it.face?.let { face -> formatFace(face) }
            it.fingerprint?.let { fp -> formatFingerprint(fp) }
            formatConsent(it.consent)
            formatIdentification(it.identification)
            formatSync(it.synchronization)
            it.multifactorId?.let { mfid -> formatMultiFactorId(mfid) }
        }
        deviceConfig?.let { formatDeviceConfig(it) }
    }.trimEnd()

    private fun StringBuilder.formatGeneral(config: ProjectConfiguration) {
        val g = config.general
        appendLine("**General**")
        appendLine("- Modalities: ${g.modalities.joinToString(", ")}")
        appendLine("- Matching modalities: ${g.matchingModalities.joinToString(", ")}")
        appendLine("- Languages: ${g.languageOptions.joinToString(", ")} (default: ${g.defaultLanguage})")
        appendLine("- Collect location: ${g.collectLocation}")
        appendLine("- Duplicate biometric check: ${g.duplicateBiometricEnrolmentCheck}")
    }

    private fun StringBuilder.formatFace(face: FaceConfiguration) {
        appendLine("**Face**")
        appendLine("- SDKs: ${face.allowedSDKs.joinToString(", ")}")
        face.rankOne?.let { sdk ->
            appendLine("- RankOne: images=${sdk.nbOfImagesToCapture}, quality=${sdk.qualityThreshold}, version=${sdk.version}")
            appendLine("  - Decision policy: ${sdk.decisionPolicy}")
            appendLine("  - Age range: ${sdk.allowedAgeRange}")
            sdk.verificationMatchThreshold?.let { appendLine("  - Verification threshold: $it") }
            appendLine("  - Image saving: ${sdk.imageSavingStrategy}")
        }
        face.simFace?.let { sdk ->
            appendLine("- SimFace: images=${sdk.nbOfImagesToCapture}, quality=${sdk.qualityThreshold}, version=${sdk.version}")
            appendLine("  - Decision policy: ${sdk.decisionPolicy}")
            appendLine("  - Age range: ${sdk.allowedAgeRange}")
            sdk.verificationMatchThreshold?.let { appendLine("  - Verification threshold: $it") }
            appendLine("  - Image saving: ${sdk.imageSavingStrategy}")
        }
    }

    private fun StringBuilder.formatFingerprint(fp: FingerprintConfiguration) {
        appendLine("**Fingerprint**")
        appendLine("- Scanners: ${fp.allowedScanners.joinToString(", ")}")
        appendLine("- SDKs: ${fp.allowedSDKs.joinToString(", ")}")
        appendLine("- Display hand icons: ${fp.displayHandIcons}")
        fp.secugenSimMatcher?.let { sdk ->
            appendLine("- SecugenSimMatcher: fingers=${sdk.fingersToCapture}, version=${sdk.version}")
            appendLine("  - Decision policy: ${sdk.decisionPolicy}")
            appendLine("  - Comparison strategy: ${sdk.comparisonStrategyForVerification}")
            appendLine("  - Age range: ${sdk.allowedAgeRange}")
            sdk.verificationMatchThreshold?.let { appendLine("  - Verification threshold: $it") }
            sdk.maxCaptureAttempts?.let { appendLine("  - Max capture attempts: $it") }
        }
        fp.nec?.let { sdk ->
            appendLine("- NEC: fingers=${sdk.fingersToCapture}, version=${sdk.version}")
            appendLine("  - Decision policy: ${sdk.decisionPolicy}")
            appendLine("  - Comparison strategy: ${sdk.comparisonStrategyForVerification}")
            appendLine("  - Age range: ${sdk.allowedAgeRange}")
            sdk.verificationMatchThreshold?.let { appendLine("  - Verification threshold: $it") }
            sdk.maxCaptureAttempts?.let { appendLine("  - Max capture attempts: $it") }
        }
    }

    private fun StringBuilder.formatConsent(consent: ConsentConfiguration) {
        appendLine("**Consent**")
        appendLine("- Program: ${consent.programName}")
        appendLine("- Organization: ${consent.organizationName}")
        appendLine("- Collect consent: ${consent.collectConsent}")
        appendLine("- Parental consent: ${consent.allowParentalConsent}")
    }

    private fun StringBuilder.formatIdentification(id: IdentificationConfiguration) {
        appendLine("**Identification**")
        appendLine("- Max returned candidates: ${id.maxNbOfReturnedCandidates}")
        appendLine("- Pool type: ${id.poolType}")
    }

    private fun StringBuilder.formatSync(sync: SynchronizationConfiguration) {
        appendLine("**Synchronization**")
        val up = sync.up.simprints
        appendLine("- Upload: kind=${up.kind}, frequency=${up.frequency}")
        appendLine("  - Images require WiFi: ${up.imagesRequireUnmeteredConnection}")
        appendLine("- CoSync upload: kind=${sync.up.coSync.kind}")
        sync.down.simprints?.let { down ->
            appendLine("- Download: partition=${down.partitionType}, maxModules=${down.maxNbOfModules}, frequency=${down.frequency}")
        }
        if (sync.down.commCare != null) {
            appendLine("- CommCare download: enabled")
        }
    }

    private fun StringBuilder.formatMultiFactorId(mfid: MultiFactorIdConfiguration) {
        appendLine("**Multi-Factor ID**")
        appendLine("- Allowed credentials: ${mfid.allowedExternalCredentials.joinToString(", ")}")
    }

    private fun StringBuilder.formatDeviceConfig(deviceConfig: DeviceConfiguration) {
        appendLine("**Device Configuration**")
        appendLine("- Language: ${deviceConfig.language}")
        if (deviceConfig.selectedModules.isNotEmpty()) {
            appendLine("- Selected modules:")
            deviceConfig.selectedModules.forEach { module ->
                appendLine("  - ${module.value}")
            }
        } else {
            appendLine("- Selected modules: (none)")
        }
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
