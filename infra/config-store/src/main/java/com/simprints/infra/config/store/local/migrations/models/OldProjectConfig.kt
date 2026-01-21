package com.simprints.infra.config.store.local.migrations.models

import androidx.annotation.Keep
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.domain.common.Modality
import com.simprints.core.domain.common.TemplateIdentifier
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.infra.config.store.models.ConsentConfiguration
import com.simprints.infra.config.store.models.DecisionPolicy
import com.simprints.infra.config.store.models.DownSynchronizationConfiguration
import com.simprints.infra.config.store.models.FaceConfiguration
import com.simprints.infra.config.store.models.FingerprintConfiguration
import com.simprints.infra.config.store.models.Frequency
import com.simprints.infra.config.store.models.GeneralConfiguration
import com.simprints.infra.config.store.models.IdentificationConfiguration
import com.simprints.infra.config.store.models.ModalitySdkType
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.models.SampleSynchronizationConfiguration
import com.simprints.infra.config.store.models.SettingsPasswordConfig
import com.simprints.infra.config.store.models.SynchronizationConfiguration
import com.simprints.infra.config.store.models.UpSynchronizationConfiguration
import com.simprints.infra.config.store.models.Vero1Configuration
import com.simprints.infra.config.store.models.Vero2Configuration
import com.simprints.infra.config.store.models.Vero2Configuration.LedsMode.BASIC
import com.simprints.infra.config.store.models.Vero2Configuration.LedsMode.LIVE_QUALITY_FEEDBACK
import com.simprints.infra.serialization.SimJson
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.JsonElement
import org.json.JSONObject

@Keep
@Serializable
@ExcludedFromGeneratedTestCoverageReports("Data class")
internal data class OldProjectConfig(
    @SerialName("CaptureFingerprintStrategy") private val captureFingerprintStrategy: String?,
    @SerialName("ConsentRequired") private val consentRequired: String,
    @SerialName("SaveFingerprintImagesStrategy") private val saveFingerprintImagesStrategy: String?,
    @SerialName("Vero2FirmwareVersions") private val vero2FirmwareVersions: String?,
    @SerialName("FingerprintQualityThreshold") private val fingerprintQualityThreshold: String?,
    @SerialName("FingerprintsToCollect") private val fingerprintsToCollect: String?,
    @SerialName("ConsentParentalExists") private val consentParentalExists: String,
    @SerialName("MaxNbOfModules") private val maxNbOfModules: String,
    @SerialName("FaceQualityThreshold") private val faceQualityThreshold: String?,
    @SerialName("LogoExists") private val logoExists: String,
    @SerialName("EnrolmentPlus") private val enrolmentPlus: String,
    @SerialName("FingerComparisonStrategyForVerification") private val fingerComparisonStrategyForVerification: String?,
    @SerialName("FingerStatus") private val fingerStatus: String?,
    @SerialName("ScannerGenerations") private val scannerGenerations: String?,
    @SerialName("FaceNbOfFramesCaptured") private val faceNbOfFramesCaptured: String?,
    @SerialName("ProjectSpecificMode") private val projectSpecificMode: String,
    @SerialName("OrganizationName") private val organizationName: String,
    @SerialName("SelectedLanguage") private val selectedLanguage: String,
    @SerialName("ProjectLanguages") private val projectLanguages: String,
    @SerialName("FingerprintLiveFeedbackOn") private val fingerprintLiveFeedbackOn: String?,
    @SerialName("FaceConfidenceThresholds") private val faceConfidenceThresholds: String?,
    @SerialName("MatchGroup") private val matchGroup: String,
    @SerialName("SyncDestination") private val syncDestination: String?,
    @SerialName("SimprintsSync") private val simprintsSync: String?,
    @SerialName("LocationRequired") private val locationRequired: String,
    @SerialName("FingerImagesExist") private val fingerImagesExist: String?,
    @SerialName("ConsentParentalOptions") private val consentParentalOptions: String,
    @SerialName("SaveFaceImages") private val saveFaceImages: String?,
    @SerialName("ConsentGeneralOptions") private val consentGeneralOptions: String,
    @SerialName("DownSyncSetting") private val downSyncSetting: String,
    @SerialName("CoSync") private val coSync: String?,
    @SerialName("ModuleIdOptions") private val moduleIdOptions: String,
    @SerialName("FingerprintConfidenceThresholds") private val fingerprintConfidenceThresholds: String?,
    @SerialName("ProgramName") private val programName: String,
    @SerialName("SyncGroup") private val syncGroup: String,
    @SerialName("NbOfIdsInt") private val nbOfIdsInt: String,
    @SerialName("Modality") private val modality: String,
    @SerialName("Custom") private val custom: JsonElement?,
) {
    fun toDomain(projectId: String): ProjectConfiguration = ProjectConfiguration(
        id = "",
        projectId = projectId,
        updatedAt = "",
        general = generalConfiguration(),
        face = faceConfiguration(),
        fingerprint = fingerprintConfiguration(),
        consent = consentConfiguration(),
        identification = identificationConfiguration(),
        synchronization = synchronizationConfiguration(),
        multifactorId = null,
        custom = null,
    )

    private fun generalConfiguration(): GeneralConfiguration {
        val modalities = modality.split(",").map { if (it == "FINGER") "FINGERPRINT" else it }.map { Modality.valueOf(it) }
        return GeneralConfiguration(
            modalities = modalities,
            matchingModalities = modalities,
            languageOptions = projectLanguages.split(","),
            defaultLanguage = selectedLanguage,
            collectLocation = locationRequired.toBoolean(),
            duplicateBiometricEnrolmentCheck = enrolmentPlus.toBoolean(),
            settingsPassword = SettingsPasswordConfig.NotSet,
        )
    }

    private fun faceConfiguration(): FaceConfiguration? = if (faceQualityThreshold == null) {
        null
    } else {
        FaceConfiguration(
            allowedSDKs = listOf(ModalitySdkType.RANK_ONE),
            rankOne = FaceConfiguration.FaceSdkConfiguration(
                nbOfImagesToCapture = faceNbOfFramesCaptured?.toIntOrNull() ?: DEFAULT_FACE_FRAMES_TO_CAPTURE,
                qualityThreshold = faceQualityThreshold.toFloat(),
                imageSavingStrategy = if (saveFaceImages.toBoolean()) {
                    FaceConfiguration.ImageSavingStrategy.ONLY_USED_IN_REFERENCE
                } else {
                    FaceConfiguration.ImageSavingStrategy.NEVER
                },
                decisionPolicy = faceConfidenceThresholds?.let { parseDecisionPolicy(it) } ?: DecisionPolicy(0, 0, 0),
                version = DEFAULT_FACE_SDK_VERSION,
            ),
            simFace = null,
        )
    }

    private fun fingerprintConfiguration(): FingerprintConfiguration? = if (fingerprintQualityThreshold == null) {
        null
    } else {
        FingerprintConfiguration(
            allowedSDKs = listOf(ModalitySdkType.SECUGEN_SIM_MATCHER),
            displayHandIcons = fingerImagesExist.toBoolean(),
            allowedScanners = scannerGenerations?.split(",")?.map {
                FingerprintConfiguration.VeroGeneration.valueOf(
                    it,
                )
            } ?: listOf(FingerprintConfiguration.VeroGeneration.VERO_1),
            secugenSimMatcher = FingerprintConfiguration.FingerprintSdkConfiguration(
                fingersToCapture = fingerprintsToCollect?.split(",")?.map { TemplateIdentifier.valueOf(it) } ?: listOf(
                    TemplateIdentifier.LEFT_THUMB,
                    TemplateIdentifier.LEFT_INDEX_FINGER,
                ),
                decisionPolicy = fingerprintConfidenceThresholds?.let { parseDecisionPolicy(it) } ?: DecisionPolicy(0, 0, 700),
                comparisonStrategyForVerification = fingerComparisonStrategyForVerification?.let {
                    FingerprintConfiguration.FingerComparisonStrategy.valueOf(it)
                } ?: FingerprintConfiguration.FingerComparisonStrategy.SAME_FINGER,
                vero1 = Vero1Configuration(fingerprintQualityThreshold.toInt()),
                vero2 = vero2Configuration(),
                maxCaptureAttempts = null,
            ),
            nec = null,
        )
    }

    private fun vero2Configuration(): Vero2Configuration? = if (captureFingerprintStrategy == null) {
        null
    } else {
        Vero2Configuration(
            fingerprintQualityThreshold!!.toInt(),
            captureStrategy = Vero2Configuration.CaptureStrategy.valueOf(
                captureFingerprintStrategy,
            ),
            ledsMode = if (fingerprintLiveFeedbackOn.toBoolean()) LIVE_QUALITY_FEEDBACK else BASIC,
            imageSavingStrategy = when (saveFingerprintImagesStrategy) {
                "NEVER" -> Vero2Configuration.ImageSavingStrategy.NEVER
                "WSQ_15" -> Vero2Configuration.ImageSavingStrategy.ONLY_USED_IN_REFERENCE
                "WSQ_15_EAGER" -> Vero2Configuration.ImageSavingStrategy.EAGER
                else -> Vero2Configuration.ImageSavingStrategy.NEVER
            },
            firmwareVersions = if (vero2FirmwareVersions.isNullOrEmpty()) {
                emptyMap()
            } else {
                // Construct a JavaType instance for Map<String, Vero2FirmwareVersions>
                SimJson.decodeFromString(
                    MapSerializer(
                        String.serializer(),
                        Vero2Configuration.Vero2FirmwareVersions.serializer(),
                    ),
                    vero2FirmwareVersions,
                )
            },
        )
    }

    private fun consentConfiguration(): ConsentConfiguration = ConsentConfiguration(
        programName = programName,
        organizationName = organizationName,
        collectConsent = consentRequired.toBoolean(),
        displaySimprintsLogo = logoExists.toBoolean(),
        allowParentalConsent = consentParentalExists.toBoolean(),
        generalPrompt = SimJson.decodeFromString<GeneralConsentOptions>(consentGeneralOptions).toDomain(),
        parentalPrompt = SimJson.decodeFromString<ParentalConsentOptions>(consentParentalOptions).toDomain(),
    )

    private fun identificationConfiguration(): IdentificationConfiguration = IdentificationConfiguration(
        maxNbOfReturnedCandidates = nbOfIdsInt.toInt(),
        poolType = IdentificationConfiguration.PoolType.valueOf(
            if (matchGroup == "GLOBAL") "PROJECT" else matchGroup,
        ),
    )

    private fun synchronizationConfiguration(): SynchronizationConfiguration = SynchronizationConfiguration(
        up = UpSynchronizationConfiguration(
            simprints = UpSynchronizationConfiguration.SimprintsUpSynchronizationConfiguration(
                kind = if (simprintsSync == null) {
                    if (syncDestination?.contains("SIMPRINTS") == true) {
                        UpSynchronizationConfiguration.UpSynchronizationKind.ALL
                    } else {
                        UpSynchronizationConfiguration.UpSynchronizationKind.NONE
                    }
                } else {
                    UpSynchronizationConfiguration.UpSynchronizationKind.valueOf(
                        simprintsSync,
                    )
                },
                batchSizes = UpSynchronizationConfiguration.UpSyncBatchSizes(
                    sessions = DEFAULT_BATCH_SIZE,
                    eventUpSyncs = DEFAULT_BATCH_SIZE,
                    eventDownSyncs = DEFAULT_BATCH_SIZE,
                    sampleUpSyncs = DEFAULT_BATCH_SIZE,
                ),
                imagesRequireUnmeteredConnection = false,
                frequency = when (downSyncSetting) {
                    "OFF" -> Frequency.ONLY_PERIODICALLY_UP_SYNC
                    "ON" -> Frequency.PERIODICALLY
                    "EXTRA" -> Frequency.PERIODICALLY_AND_ON_SESSION_START
                    else -> Frequency.PERIODICALLY
                },
            ),
            coSync = UpSynchronizationConfiguration.CoSyncUpSynchronizationConfiguration(
                kind = if (coSync == null) {
                    if (syncDestination?.contains("COMMCARE") == true) {
                        UpSynchronizationConfiguration.UpSynchronizationKind.ALL
                    } else {
                        UpSynchronizationConfiguration.UpSynchronizationKind.NONE
                    }
                } else {
                    UpSynchronizationConfiguration.UpSynchronizationKind.valueOf(
                        coSync,
                    )
                },
            ),
        ),
        down = DownSynchronizationConfiguration(
            simprints = DownSynchronizationConfiguration.SimprintsDownSynchronizationConfiguration(
                partitionType = DownSynchronizationConfiguration.PartitionType.valueOf(
                    if (syncGroup == "GLOBAL") "PROJECT" else syncGroup,
                ),
                maxNbOfModules = maxNbOfModules.toInt(),
                moduleOptions = moduleIdOptions.split("|").map(String::asTokenizableRaw),
                maxAge = DownSynchronizationConfiguration.DEFAULT_DOWN_SYNC_MAX_AGE,
                frequency = when (downSyncSetting) {
                    "OFF" -> Frequency.ONLY_PERIODICALLY_UP_SYNC
                    "ON" -> Frequency.PERIODICALLY
                    "EXTRA" -> Frequency.PERIODICALLY_AND_ON_SESSION_START
                    else -> Frequency.PERIODICALLY
                },
            ),
            commCare = null,
        ),
        samples = SampleSynchronizationConfiguration(
            signedUrlBatchSize = DEFAULT_BATCH_SIZE,
        ),
    )

    private fun parseDecisionPolicy(decisionPolicy: String): DecisionPolicy = with(JSONObject(decisionPolicy)) {
        DecisionPolicy(
            low = getString("LOW").toInt(),
            medium = getString("MEDIUM").toInt(),
            high = getString("HIGH").toInt(),
        )
    }

    companion object {
        private const val DEFAULT_FACE_FRAMES_TO_CAPTURE = 2
        private const val DEFAULT_FACE_SDK_VERSION = "1.23"
        private const val DEFAULT_BATCH_SIZE = 1
    }
}
