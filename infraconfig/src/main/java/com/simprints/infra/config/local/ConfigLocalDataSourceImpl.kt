package com.simprints.infra.config.local

import androidx.datastore.core.DataStore
import com.simprints.core.tools.utils.LanguageHelper
import com.simprints.infra.config.AbsolutePath
import com.simprints.infra.config.domain.models.*
import com.simprints.infra.config.domain.models.Project
import com.simprints.infra.config.local.models.*
import com.simprints.infra.config.local.models.ProtoDeviceConfiguration.FingersToCollect
import kotlinx.coroutines.flow.first
import java.io.File
import javax.inject.Inject

internal class ConfigLocalDataSourceImpl @Inject constructor(
    @AbsolutePath private val absolutePath: String,
    private val projectDataStore: DataStore<ProtoProject>,
    private val configDataStore: DataStore<ProtoProjectConfiguration>,
    private val deviceConfigDataStore: DataStore<ProtoDeviceConfiguration>
) : ConfigLocalDataSource {

    override suspend fun saveProject(project: Project) {
        projectDataStore.updateData { project.toProto() }
    }

    override suspend fun getProject(): Project =
        projectDataStore.data.first().toDomain().also {
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
                            it.language.toBuilder().setLanguage(config.general.defaultLanguage)
                        ).build()
                    LanguageHelper.language = it.language.language
                }
                if (!protoDeviceConfiguration.fingersToCollect.isOverwritten) {
                    proto.setFingersToCollect(
                        FingersToCollect.newBuilder()
                            .addAllFingersToCollect(config.fingerprint?.fingersToCapture?.map { finger -> finger.toProto() }
                                ?: listOf())
                    ).build()
                }
                proto.build()
            }
        }
    }

    override suspend fun getProjectConfiguration(): ProjectConfiguration =
        configDataStore.data.first().toDomain()

    override suspend fun clearProjectConfiguration() {
        configDataStore.updateData { it.toBuilder().clear().build() }
    }

    override suspend fun getDeviceConfiguration(): DeviceConfiguration =
        deviceConfigDataStore.data.first().toDomain()

    override suspend fun updateDeviceConfiguration(update: suspend (t: DeviceConfiguration) -> DeviceConfiguration) {
        deviceConfigDataStore.updateData { currentData ->
            val updatedProto = update(currentData.toDomain()).toProto()
            val updatedProtoBuilder = updatedProto.toBuilder()
            if (updatedProto.language.language != currentData.language.language) {
                updatedProtoBuilder.language =
                    updatedProto.language.toBuilder().setIsOverwritten(true).build()
                LanguageHelper.language = updatedProto.language.language
            }
            if (updatedProto.fingersToCollect.fingersToCollectList != currentData.fingersToCollect.fingersToCollectList) {
                updatedProtoBuilder.fingersToCollect =
                    updatedProto.fingersToCollect.toBuilder().setIsOverwritten(true).build()
            }
            updatedProtoBuilder.build()
        }
    }

    override suspend fun clearDeviceConfiguration() {
        deviceConfigDataStore.updateData { it.toBuilder().clear().build() }
    }

    override fun storePrivacyNotice(projectId: String, language: String, content: String) {
        val projectDir = File(filePathForPrivacyNoticeDirectory(projectId))
        if (!projectDir.exists()) {
            projectDir.mkdirs()
        }
        val privacyNoticeFile = fileForPrivacyNotice(projectId, language)
        privacyNoticeFile.writeText(content)
    }

    override fun getPrivacyNotice(projectId: String, language: String): String {
        return fileForPrivacyNotice(projectId, language).readText()
    }

    override fun hasPrivacyNoticeFor(projectId: String, language: String): Boolean =
        fileForPrivacyNotice(projectId, language).exists()

    override fun deletePrivacyNotices() {
        File("$absolutePath${File.separator}$PRIVACY_NOTICE_FOLDER").deleteRecursively()
    }

    private fun fileForPrivacyNotice(projectId: String, language: String): File =
        File(filePathForPrivacyNoticeDirectory(projectId), "$language.$FILE_TYPE")

    private fun filePathForPrivacyNoticeDirectory(projectId: String): String =
        "$absolutePath${File.separator}$PRIVACY_NOTICE_FOLDER${File.separator}$projectId"

    companion object {
        val defaultProjectConfiguration: ProtoProjectConfiguration =
            ProjectConfiguration(
                projectId = "",
                general = GeneralConfiguration(
                    modalities = listOf(GeneralConfiguration.Modality.FINGERPRINT),
                    languageOptions = listOf(),
                    defaultLanguage = "en",
                    collectLocation = true,
                    duplicateBiometricEnrolmentCheck = false,
                ),
                face = null,
                fingerprint = FingerprintConfiguration(
                    fingersToCapture = listOf(
                        Finger.LEFT_THUMB,
                        Finger.LEFT_INDEX_FINGER
                    ),
                    qualityThreshold = 60,
                    decisionPolicy = DecisionPolicy(0, 0, 700),
                    allowedVeroGenerations = listOf(FingerprintConfiguration.VeroGeneration.VERO_1),
                    comparisonStrategyForVerification = FingerprintConfiguration.FingerComparisonStrategy.SAME_FINGER,
                    displayHandIcons = true,
                    vero2 = null,
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
                    frequency = SynchronizationConfiguration.Frequency.PERIODICALLY,
                    up = UpSynchronizationConfiguration(
                        simprints = UpSynchronizationConfiguration.SimprintsUpSynchronizationConfiguration(
                            kind = UpSynchronizationConfiguration.UpSynchronizationKind.NONE
                        ),
                        coSync = UpSynchronizationConfiguration.CoSyncUpSynchronizationConfiguration(
                            kind = UpSynchronizationConfiguration.UpSynchronizationKind.NONE
                        )
                    ),
                    down = DownSynchronizationConfiguration(
                        partitionType = DownSynchronizationConfiguration.PartitionType.USER,
                        maxNbOfModules = 6,
                        moduleOptions = listOf(),
                    ),
                ),
            ).toProto()
        val defaultDeviceConfiguration: ProtoDeviceConfiguration = DeviceConfiguration(
            language = "",
            selectedModules = listOf(),
            fingersToCollect = listOf(),
            lastInstructionId = ""
        ).toProto()

        private const val PRIVACY_NOTICE_FOLDER = "long-consents"
        private const val FILE_TYPE = "txt"
    }

}
