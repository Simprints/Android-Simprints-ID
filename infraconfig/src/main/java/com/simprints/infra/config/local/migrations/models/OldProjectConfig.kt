package com.simprints.infra.config.local.migrations.models

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.simprints.infra.config.domain.models.*
import org.json.JSONObject

internal data class OldProjectConfig(
    private val CaptureFingerprintStrategy: String?,
    private val ConsentRequired: String,
    private val SaveFingerprintImagesStrategy: String?,
    private val Vero2FirmwareVersions: String?,
    private val FingerprintQualityThreshold: String?,
    private val FingerprintsToCollect: String?,
    private val ConsentParentalExists: String,
    private val MaxNbOfModules: String,
    private val FaceQualityThreshold: String?,
    private val LogoExists: String,
    private val EnrolmentPlus: String,
    private val FingerComparisonStrategyForVerification: String?,
    private val FingerStatus: String?,
    private val ScannerGenerations: String?,
    private val FaceNbOfFramesCaptured: String?,
    private val ProjectSpecificMode: String,
    private val OrganizationName: String,
    private val SelectedLanguage: String,
    private val ProjectLanguages: String,
    private val FingerprintLiveFeedbackOn: String?,
    private val FaceConfidenceThresholds: String?,
    private val MatchGroup: String,
    private val SyncDestination: String,
    private val SimprintsSync: String?,
    private val LocationRequired: String,
    private val FingerImagesExist: String?,
    private val ConsentParentalOptions: String,
    private val SaveFaceImages: String?,
    private val ConsentGeneralOptions: String,
    private val DownSyncSetting: String,
    private val CoSync: String?,
    private val ModuleIdOptions: String,
    private val FingerprintConfidenceThresholds: String?,
    private val ProgramName: String,
    private val SyncGroup: String,
    private val NbOfIdsInt: String,
    private val Modality: String,
) {
    fun toDomain(projectId: String): ProjectConfiguration =
        ProjectConfiguration(
            projectId = projectId,
            general = generalConfiguration(),
            face = faceConfiguration(),
            fingerprint = fingerprintConfiguration(),
            consent = consentConfiguration(),
            identification = identificationConfiguration(),
            synchronization = synchronizationConfiguration(),
        )

    private fun generalConfiguration(): GeneralConfiguration =
        GeneralConfiguration(
            modalities = Modality.split(",")
                .map { if (it == "FINGER") "FINGERPRINT" else it }
                .map { GeneralConfiguration.Modality.valueOf(it) },
            languageOptions = ProjectLanguages.split(","),
            defaultLanguage = SelectedLanguage,
            collectLocation = LocationRequired.toBoolean(),
            duplicateBiometricEnrolmentCheck = EnrolmentPlus.toBoolean(),
        )

    private fun faceConfiguration(): FaceConfiguration? =
        if (FaceQualityThreshold == null) null
        else
            FaceConfiguration(
                nbOfImagesToCapture = FaceNbOfFramesCaptured!!.toInt(),
                qualityThreshold = FaceQualityThreshold.toInt(),
                imageSavingStrategy = if (SaveFaceImages.toBoolean()) {
                    FaceConfiguration.ImageSavingStrategy.ONLY_GOOD_SCAN
                } else {
                    FaceConfiguration.ImageSavingStrategy.NEVER
                },
                decisionPolicy = parseDecisionPolicy(FaceConfidenceThresholds!!),
            )

    private fun fingerprintConfiguration(): FingerprintConfiguration? =
        if (FingerprintQualityThreshold == null) null
        else
            FingerprintConfiguration(
                fingersToCapture = FingerprintsToCollect!!.split(",")
                    .map { FingerprintConfiguration.Finger.valueOf(it) },
                qualityThreshold = FingerprintQualityThreshold.toInt(),
                decisionPolicy = parseDecisionPolicy(FingerprintConfidenceThresholds!!),
                allowedVeroGenerations = ScannerGenerations!!.split(",")
                    .map { FingerprintConfiguration.VeroGeneration.valueOf(it) },
                comparisonStrategyForVerification = FingerComparisonStrategyForVerification?.let {
                    FingerprintConfiguration.FingerComparisonStrategy.valueOf(
                        it
                    )
                } ?: FingerprintConfiguration.FingerComparisonStrategy.SAME_FINGER,
                displayHandIcons = FingerImagesExist.toBoolean(),
                vero2 = vero2Configuration(),
            )

    private fun vero2Configuration(): Vero2Configuration? =
        if (CaptureFingerprintStrategy == null) null
        else
            Vero2Configuration(
                captureStrategy = Vero2Configuration.CaptureStrategy.valueOf(
                    CaptureFingerprintStrategy
                ),
                displayLiveFeedback = FingerprintLiveFeedbackOn.toBoolean(),
                imageSavingStrategy = when (SaveFingerprintImagesStrategy) {
                    "NEVER" -> Vero2Configuration.ImageSavingStrategy.NEVER
                    "WSQ_15" -> Vero2Configuration.ImageSavingStrategy.ONLY_GOOD_SCAN
                    "WSQ_15_EAGER" -> Vero2Configuration.ImageSavingStrategy.EAGER
                    else -> Vero2Configuration.ImageSavingStrategy.NEVER
                },
                firmwareVersions = if (Vero2FirmwareVersions.isNullOrEmpty()) {
                    emptyMap()
                } else {
                    ObjectMapper().readValue(Vero2FirmwareVersions)
                },
            )

    private fun consentConfiguration(): ConsentConfiguration =
        ConsentConfiguration(
            programName = ProgramName,
            organizationName = OrganizationName,
            collectConsent = ConsentRequired.toBoolean(),
            displaySimprintsLogo = LogoExists.toBoolean(),
            allowParentalConsent = ConsentParentalExists.toBoolean(),
            generalPrompt = fromJson<GeneralConsentOptions>(ConsentGeneralOptions).toDomain(),
            parentalPrompt = fromJson<ParentalConsentOptions>(ConsentParentalOptions).toDomain(),
        )

    private fun identificationConfiguration(): IdentificationConfiguration =
        IdentificationConfiguration(
            maxNbOfReturnedCandidates = NbOfIdsInt.toInt(),
            poolType = IdentificationConfiguration.PoolType.valueOf(
                if (MatchGroup == "GLOBAL") "PROJECT" else MatchGroup
            ),
        )

    private fun synchronizationConfiguration(): SynchronizationConfiguration =
        SynchronizationConfiguration(
            frequency = when (DownSyncSetting) {
                "OFF" -> SynchronizationConfiguration.Frequency.ONLY_PERIODICALLY_UP_SYNC
                "ON" -> SynchronizationConfiguration.Frequency.PERIODICALLY
                "EXTRA" -> SynchronizationConfiguration.Frequency.PERIODICALLY_AND_ON_SESSION_START
                else -> SynchronizationConfiguration.Frequency.PERIODICALLY
            },
            up = UpSynchronizationConfiguration(
                simprints = UpSynchronizationConfiguration.SimprintsUpSynchronizationConfiguration(
                    kind = if (SimprintsSync == null) {
                        if (SyncDestination.contains("SIMPRINTS")) {
                            UpSynchronizationConfiguration.UpSynchronizationKind.ALL
                        } else {
                            UpSynchronizationConfiguration.UpSynchronizationKind.NONE
                        }
                    } else {
                        UpSynchronizationConfiguration.UpSynchronizationKind.valueOf(SimprintsSync)
                    }
                ),
                coSync = UpSynchronizationConfiguration.CoSyncUpSynchronizationConfiguration(
                    kind = if (CoSync == null) {
                        if (SyncDestination.contains("COMMCARE")) {
                            UpSynchronizationConfiguration.UpSynchronizationKind.ALL
                        } else {
                            UpSynchronizationConfiguration.UpSynchronizationKind.NONE
                        }
                    } else {
                        UpSynchronizationConfiguration.UpSynchronizationKind.valueOf(CoSync)
                    }
                )
            ),
            down = DownSynchronizationConfiguration(
                partitionType = DownSynchronizationConfiguration.PartitionType.valueOf(
                    if (SyncGroup == "GLOBAL") "PROJECT" else SyncGroup
                ),
                maxNbOfModules = MaxNbOfModules.toInt(),
                moduleOptions = ModuleIdOptions.split("|")
            ),
        )

    private fun parseDecisionPolicy(decisionPolicy: String): DecisionPolicy =
        with(JSONObject(decisionPolicy)) {
            DecisionPolicy(
                low = getString("LOW").toInt(),
                medium = getString("MEDIUM").toInt(),
                high = getString("HIGH").toInt(),
            )
        }

    private inline fun <reified T> fromJson(json: String): T {
        return jacksonObjectMapper().readValue(json, T::class.java)
    }
}

