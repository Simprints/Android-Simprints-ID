package com.simprints.infra.config.store.local.migrations.models

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonProperty
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.core.tools.json.JsonHelper
import com.simprints.infra.config.store.models.ConsentConfiguration
import com.simprints.infra.config.store.models.DecisionPolicy
import com.simprints.infra.config.store.models.DownSynchronizationConfiguration
import com.simprints.infra.config.store.models.FaceConfiguration
import com.simprints.infra.config.store.models.Finger
import com.simprints.infra.config.store.models.FingerprintConfiguration
import com.simprints.infra.config.store.models.Frequency
import com.simprints.infra.config.store.models.GeneralConfiguration
import com.simprints.infra.config.store.models.IdentificationConfiguration
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.models.SampleSynchronizationConfiguration
import com.simprints.infra.config.store.models.SettingsPasswordConfig
import com.simprints.infra.config.store.models.SynchronizationConfiguration
import com.simprints.infra.config.store.models.UpSynchronizationConfiguration
import com.simprints.infra.config.store.models.Vero1Configuration
import com.simprints.infra.config.store.models.Vero2Configuration
import com.simprints.infra.config.store.models.Vero2Configuration.LedsMode.BASIC
import com.simprints.infra.config.store.models.Vero2Configuration.LedsMode.LIVE_QUALITY_FEEDBACK
import org.json.JSONObject

@Keep
internal data class OldProjectConfig(
    @JsonProperty("CaptureFingerprintStrategy") private val captureFingerprintStrategy: String?,
    @JsonProperty("ConsentRequired") private val consentRequired: String,
    @JsonProperty("SaveFingerprintImagesStrategy") private val saveFingerprintImagesStrategy: String?,
    @JsonProperty("Vero2FirmwareVersions") private val vero2FirmwareVersions: String?,
    @JsonProperty("FingerprintQualityThreshold") private val fingerprintQualityThreshold: String?,
    @JsonProperty("FingerprintsToCollect") private val fingerprintsToCollect: String?,
    @JsonProperty("ConsentParentalExists") private val consentParentalExists: String,
    @JsonProperty("MaxNbOfModules") private val maxNbOfModules: String,
    @JsonProperty("FaceQualityThreshold") private val faceQualityThreshold: String?,
    @JsonProperty("LogoExists") private val logoExists: String,
    @JsonProperty("EnrolmentPlus") private val enrolmentPlus: String,
    @JsonProperty("FingerComparisonStrategyForVerification") private val fingerComparisonStrategyForVerification: String?,
    @JsonProperty("FingerStatus") private val fingerStatus: String?,
    @JsonProperty("ScannerGenerations") private val scannerGenerations: String?,
    @JsonProperty("FaceNbOfFramesCaptured") private val faceNbOfFramesCaptured: String?,
    @JsonProperty("ProjectSpecificMode") private val projectSpecificMode: String,
    @JsonProperty("OrganizationName") private val organizationName: String,
    @JsonProperty("SelectedLanguage") private val selectedLanguage: String,
    @JsonProperty("ProjectLanguages") private val projectLanguages: String,
    @JsonProperty("FingerprintLiveFeedbackOn") private val fingerprintLiveFeedbackOn: String?,
    @JsonProperty("FaceConfidenceThresholds") private val faceConfidenceThresholds: String?,
    @JsonProperty("MatchGroup") private val matchGroup: String,
    @JsonProperty("SyncDestination") private val syncDestination: String?,
    @JsonProperty("SimprintsSync") private val simprintsSync: String?,
    @JsonProperty("LocationRequired") private val locationRequired: String,
    @JsonProperty("FingerImagesExist") private val fingerImagesExist: String?,
    @JsonProperty("ConsentParentalOptions") private val consentParentalOptions: String,
    @JsonProperty("SaveFaceImages") private val saveFaceImages: String?,
    @JsonProperty("ConsentGeneralOptions") private val consentGeneralOptions: String,
    @JsonProperty("DownSyncSetting") private val downSyncSetting: String,
    @JsonProperty("CoSync") private val coSync: String?,
    @JsonProperty("ModuleIdOptions") private val moduleIdOptions: String,
    @JsonProperty("FingerprintConfidenceThresholds") private val fingerprintConfidenceThresholds: String?,
    @JsonProperty("ProgramName") private val programName: String,
    @JsonProperty("SyncGroup") private val syncGroup: String,
    @JsonProperty("NbOfIdsInt") private val nbOfIdsInt: String,
    @JsonProperty("Modality") private val modality: String,
    @JsonProperty("Custom") private val custom: Any?,
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
        custom = null,
    )

    private fun generalConfiguration(): GeneralConfiguration {
        val modalities = modality
            .split(",")
            .map { if (it == "FINGER") "FINGERPRINT" else it }
            .map {
                GeneralConfiguration.Modality.valueOf(
                    it,
                )
            }
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
            allowedSDKs = listOf(FaceConfiguration.BioSdk.RANK_ONE),
            rankOne = FaceConfiguration.FaceSdkConfiguration(
                nbOfImagesToCapture = faceNbOfFramesCaptured?.toIntOrNull()
                    ?: DEFAULT_FACE_FRAMES_TO_CAPTURE,
                qualityThreshold = faceQualityThreshold.toFloat(),
                imageSavingStrategy = if (saveFaceImages.toBoolean()) {
                    FaceConfiguration.ImageSavingStrategy.ONLY_USED_IN_REFERENCE
                } else {
                    FaceConfiguration.ImageSavingStrategy.NEVER
                },
                decisionPolicy = faceConfidenceThresholds?.let { parseDecisionPolicy(it) }
                    ?: DecisionPolicy(0, 0, 0),
                version = DEFAULT_FACE_SDK_VERSION,
            ),
            simFace = null,
        )
    }

    private fun fingerprintConfiguration(): FingerprintConfiguration? = if (fingerprintQualityThreshold == null) {
        null
    } else {
        FingerprintConfiguration(
            allowedSDKs = listOf(FingerprintConfiguration.BioSdk.SECUGEN_SIM_MATCHER),
            displayHandIcons = fingerImagesExist.toBoolean(),
            allowedScanners = scannerGenerations
                ?.split(",")
                ?.map {
                    FingerprintConfiguration.VeroGeneration.valueOf(
                        it,
                    )
                }
                ?: listOf(FingerprintConfiguration.VeroGeneration.VERO_1),
            secugenSimMatcher = FingerprintConfiguration.FingerprintSdkConfiguration(
                fingersToCapture = fingerprintsToCollect
                    ?.split(",")
                    ?.map { Finger.valueOf(it) }
                    ?: listOf(
                        Finger.LEFT_THUMB,
                        Finger.LEFT_INDEX_FINGER,
                    ),
                decisionPolicy = fingerprintConfidenceThresholds?.let { parseDecisionPolicy(it) }
                    ?: DecisionPolicy(0, 0, 700),
                comparisonStrategyForVerification = fingerComparisonStrategyForVerification
                    ?.let {
                        FingerprintConfiguration.FingerComparisonStrategy.valueOf(it)
                    }
                    ?: FingerprintConfiguration.FingerComparisonStrategy.SAME_FINGER,
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
                val type = JsonHelper.jackson.typeFactory.constructMapType(
                    Map::class.java,
                    String::class.java,
                    Vero2Configuration.Vero2FirmwareVersions::class.java,
                )
                JsonHelper.fromJson(vero2FirmwareVersions, type)
            },
        )
    }

    private fun consentConfiguration(): ConsentConfiguration = ConsentConfiguration(
        programName = programName,
        organizationName = organizationName,
        collectConsent = consentRequired.toBoolean(),
        displaySimprintsLogo = logoExists.toBoolean(),
        allowParentalConsent = consentParentalExists.toBoolean(),
        generalPrompt = JsonHelper
            .fromJson<GeneralConsentOptions>(consentGeneralOptions)
            .toDomain(),
        parentalPrompt = JsonHelper
            .fromJson<ParentalConsentOptions>(consentParentalOptions)
            .toDomain(),
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
