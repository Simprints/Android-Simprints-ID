package com.simprints.infra.config.store.local

import androidx.datastore.core.DataStore
import com.simprints.core.domain.common.Modality
import com.simprints.core.domain.reference.TemplateIdentifier
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.utils.LanguageHelper
import com.simprints.infra.config.store.AbsolutePath
import com.simprints.infra.config.store.local.models.ProtoDeviceConfiguration
import com.simprints.infra.config.store.local.models.ProtoProject
import com.simprints.infra.config.store.local.models.ProtoProjectConfiguration
import com.simprints.infra.config.store.local.models.toDomain
import com.simprints.infra.config.store.local.models.toProto
import com.simprints.infra.config.store.models.ConsentConfiguration
import com.simprints.infra.config.store.models.DecisionPolicy
import com.simprints.infra.config.store.models.DeviceConfiguration
import com.simprints.infra.config.store.models.DownSynchronizationConfiguration
import com.simprints.infra.config.store.models.DownSynchronizationConfiguration.Companion.DEFAULT_DOWN_SYNC_MAX_AGE
import com.simprints.infra.config.store.models.FingerprintConfiguration
import com.simprints.infra.config.store.models.Frequency
import com.simprints.infra.config.store.models.GeneralConfiguration
import com.simprints.infra.config.store.models.IdentificationConfiguration
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.models.SampleSynchronizationConfiguration
import com.simprints.infra.config.store.models.SettingsPasswordConfig
import com.simprints.infra.config.store.models.SynchronizationConfiguration
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.config.store.models.UpSynchronizationConfiguration
import com.simprints.infra.config.store.models.Vero1Configuration
import com.simprints.infra.config.store.tokenization.TokenizationProcessor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.File
import javax.inject.Inject

internal class ConfigLocalDataSourceImpl @Inject constructor(
    @AbsolutePath private val absolutePath: String,
    private val projectDataStore: DataStore<ProtoProject>,
    private val configDataStore: DataStore<ProtoProjectConfiguration>,
    private val deviceConfigDataStore: DataStore<ProtoDeviceConfiguration>,
    private val tokenizationProcessor: TokenizationProcessor,
) : ConfigLocalDataSource {
    override suspend fun saveProject(project: Project) {
        projectDataStore.updateData { project.toProto() }
    }

    override suspend fun getProject(): Project = projectDataStore.data.first().toDomain().also {
        if (it.id == "") {
            throw NoSuchElementException()
        }
    }

    override suspend fun clearProject() {
        projectDataStore.updateData { it.toBuilder().clear().build() }
    }

    override suspend fun saveProjectConfiguration(config: ProjectConfiguration) {
        configDataStore.updateData { config.toProto() }
        // We need to update the device configuration only for the non overwritten fields
        deviceConfigDataStore.updateData { protoDeviceConfiguration ->
            protoDeviceConfiguration.let {
                val proto = it.toBuilder()
                if (!protoDeviceConfiguration.language.isOverwritten) {
                    proto
                        .setLanguage(
                            it.language.toBuilder().setLanguage(config.general.defaultLanguage),
                        ).build()
                    LanguageHelper.language = it.language.language
                }
                proto.build()
            }
        }
    }

    override suspend fun getProjectConfiguration(): ProjectConfiguration = configDataStore.data.first().toDomain()

    override fun observeProjectConfiguration(): Flow<ProjectConfiguration> = configDataStore.data.map(ProtoProjectConfiguration::toDomain)

    override suspend fun clearProjectConfiguration() {
        configDataStore.updateData { it.toBuilder().clear().build() }
    }

    override suspend fun getDeviceConfiguration(): DeviceConfiguration = deviceConfigDataStore.data.first().toDomain().apply {
        selectedModules = selectedModules.mapToTokenizedModuleIds()
    }

    override fun observeDeviceConfiguration(): Flow<DeviceConfiguration> =
        deviceConfigDataStore.data.map(ProtoDeviceConfiguration::toDomain).map { config ->
            config.apply {
                selectedModules = selectedModules.mapToTokenizedModuleIds()
            }
        }

    private suspend fun List<TokenizableString>.mapToTokenizedModuleIds() = map { moduleId ->
        when (moduleId) {
            is TokenizableString.Raw -> tokenizationProcessor.encrypt(
                decrypted = moduleId,
                tokenKeyType = TokenKeyType.ModuleId,
                project = getProject(),
            )

            is TokenizableString.Tokenized -> moduleId
        }
    }

    override suspend fun updateDeviceConfiguration(update: suspend (t: DeviceConfiguration) -> DeviceConfiguration) {
        deviceConfigDataStore.updateData { currentData ->
            val updatedProto = update(currentData.toDomain()).toProto()
            val updatedProtoBuilder = updatedProto.toBuilder()
            if (updatedProto.language.language != currentData.language.language) {
                updatedProtoBuilder.language =
                    updatedProto.language
                        .toBuilder()
                        .setIsOverwritten(true)
                        .build()
                LanguageHelper.language = updatedProto.language.language
            }
            updatedProtoBuilder.build()
        }
    }

    override suspend fun clearDeviceConfiguration() {
        deviceConfigDataStore.updateData { it.toBuilder().clear().build() }
    }

    override fun storePrivacyNotice(
        projectId: String,
        language: String,
        content: String,
    ) {
        val projectDir = File(filePathForPrivacyNoticeDirectory(projectId))
        if (!projectDir.exists()) {
            projectDir.mkdirs()
        }
        val privacyNoticeFile = fileForPrivacyNotice(projectId, language)
        privacyNoticeFile.writeText(content)
    }

    override fun getPrivacyNotice(
        projectId: String,
        language: String,
    ): String = fileForPrivacyNotice(projectId, language).readText()

    override fun hasPrivacyNoticeFor(
        projectId: String,
        language: String,
    ): Boolean = fileForPrivacyNotice(projectId, language).exists()

    override fun deletePrivacyNotices() {
        File("$absolutePath${File.separator}$PRIVACY_NOTICE_FOLDER").deleteRecursively()
    }

    private fun fileForPrivacyNotice(
        projectId: String,
        language: String,
    ): File = File(
        filePathForPrivacyNoticeDirectory(projectId),
        "$language.$FILE_TYPE",
    )

    private fun filePathForPrivacyNoticeDirectory(projectId: String): String =
        "$absolutePath${File.separator}$PRIVACY_NOTICE_FOLDER${File.separator}$projectId"

    companion object {
        val defaultProjectConfiguration: ProtoProjectConfiguration =
            ProjectConfiguration(
                id = "",
                projectId = "",
                updatedAt = "",
                general = GeneralConfiguration(
                    modalities = listOf(Modality.FINGERPRINT),
                    matchingModalities = listOf(Modality.FINGERPRINT),
                    languageOptions = listOf(),
                    defaultLanguage = "en",
                    collectLocation = true,
                    duplicateBiometricEnrolmentCheck = false,
                    settingsPassword = SettingsPasswordConfig.NotSet,
                ),
                face = null,
                fingerprint = FingerprintConfiguration(
                    allowedScanners = listOf(FingerprintConfiguration.VeroGeneration.VERO_1),
                    displayHandIcons = true,
                    allowedSDKs = listOf(FingerprintConfiguration.BioSdk.SECUGEN_SIM_MATCHER),
                    secugenSimMatcher = FingerprintConfiguration.FingerprintSdkConfiguration(
                        fingersToCapture = listOf(
                            TemplateIdentifier.LEFT_THUMB,
                            TemplateIdentifier.LEFT_INDEX_FINGER,
                        ),
                        decisionPolicy = DecisionPolicy(
                            0,
                            0,
                            700,
                        ),
                        comparisonStrategyForVerification = FingerprintConfiguration.FingerComparisonStrategy.SAME_FINGER,
                        vero1 = Vero1Configuration(60),
                        vero2 = null,
                        version = "",
                        maxCaptureAttempts = null,
                    ),
                    nec = null,
                ),
                consent = ConsentConfiguration(
                    programName = "this program",
                    organizationName = "This organization",
                    collectConsent = true,
                    displaySimprintsLogo = true,
                    allowParentalConsent = false,
                    generalPrompt = ConsentConfiguration.ConsentPromptConfiguration(
                        enrolmentVariant = ConsentConfiguration.ConsentEnrolmentVariant.STANDARD,
                        dataSharedWithPartner = false,
                        dataUsedForRAndD = false,
                        privacyRights = true,
                        confirmation = true,
                    ),
                    parentalPrompt = null,
                ),
                identification = IdentificationConfiguration(
                    maxNbOfReturnedCandidates = 10,
                    poolType = IdentificationConfiguration.PoolType.USER,
                ),
                synchronization = SynchronizationConfiguration(
                    up = UpSynchronizationConfiguration(
                        simprints = UpSynchronizationConfiguration.SimprintsUpSynchronizationConfiguration(
                            kind = UpSynchronizationConfiguration.UpSynchronizationKind.NONE,
                            batchSizes = UpSynchronizationConfiguration.UpSyncBatchSizes.default(),
                            imagesRequireUnmeteredConnection = false,
                            frequency = Frequency.PERIODICALLY,
                        ),
                        coSync = UpSynchronizationConfiguration.CoSyncUpSynchronizationConfiguration(
                            kind = UpSynchronizationConfiguration.UpSynchronizationKind.NONE,
                        ),
                    ),
                    down = DownSynchronizationConfiguration(
                        simprints = DownSynchronizationConfiguration.SimprintsDownSynchronizationConfiguration(
                            partitionType = DownSynchronizationConfiguration.PartitionType.USER,
                            maxNbOfModules = 6,
                            moduleOptions = listOf(),
                            maxAge = DEFAULT_DOWN_SYNC_MAX_AGE,
                            frequency = Frequency.PERIODICALLY,
                        ),
                        commCare = null,
                    ),
                    samples = SampleSynchronizationConfiguration(
                        signedUrlBatchSize = 1,
                    ),
                ),
                custom = null,
                multifactorId = null,
            ).toProto()
        val defaultDeviceConfiguration: ProtoDeviceConfiguration = DeviceConfiguration(
            language = "",
            selectedModules = listOf(),
            lastInstructionId = "",
        ).toProto()

        private const val PRIVACY_NOTICE_FOLDER = "long-consents"
        private const val FILE_TYPE = "txt"
    }
}
