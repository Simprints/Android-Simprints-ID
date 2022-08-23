package com.simprints.infra.config.local

import androidx.datastore.core.DataStore
import com.simprints.infra.config.domain.models.*
import com.simprints.infra.config.domain.models.Project
import com.simprints.infra.config.local.models.*
import kotlinx.coroutines.flow.first
import javax.inject.Inject

internal class ConfigLocalDataSourceImpl @Inject constructor(
    private val projectDataStore: DataStore<ProtoProject>,
    private val configDataStore: DataStore<ProtoProjectConfiguration>,
    private val deviceConfigDataStore: DataStore<ProtoDeviceConfiguration>
) :
    ConfigLocalDataSource {
    override suspend fun saveProject(project: Project) {
        projectDataStore.updateData { project.toProto() }
    }

    override suspend fun getProject(): Project =
        projectDataStore.data.first().toDomain().also {
            if (it.id == "") {
                throw NoSuchElementException()
            }
        }

    override suspend fun saveProjectConfiguration(config: ProjectConfiguration) {
        configDataStore.updateData { config.toProto() }
    }

    override suspend fun getProjectConfiguration(): ProjectConfiguration =
        configDataStore.data.first().toDomain()

    override suspend fun getDeviceConfiguration(): DeviceConfiguration =
        deviceConfigDataStore.data.first().toDomain()

    override suspend fun updateDeviceConfiguration(update: suspend (t: DeviceConfiguration) -> DeviceConfiguration) {
        deviceConfigDataStore.updateData { update(it.toDomain()).toProto() }
    }

    companion object {
        val defaultProjectConfiguration: ProtoProjectConfiguration =
            ProjectConfiguration(
                projectId = "",
                general = GeneralConfiguration(
                    modalities = listOf(GeneralConfiguration.Modality.FINGERPRINT),
                    languageOptions = listOf(),
                    defaultLanguage = "",
                    collectLocation = true,
                    duplicateBiometricEnrolmentCheck = false,
                ),
                face = null,
                fingerprint = FingerprintConfiguration(
                    fingersToCapture = listOf(
                        FingerprintConfiguration.Finger.LEFT_THUMB,
                        FingerprintConfiguration.Finger.LEFT_INDEX_FINGER
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

    }

}
