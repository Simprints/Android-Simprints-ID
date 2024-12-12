package com.simprints.infra.sync.config.testtools

import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.infra.config.store.models.AgeGroup
import com.simprints.infra.config.store.models.ConsentConfiguration
import com.simprints.infra.config.store.models.DecisionPolicy
import com.simprints.infra.config.store.models.DownSynchronizationConfiguration
import com.simprints.infra.config.store.models.FaceConfiguration
import com.simprints.infra.config.store.models.Finger
import com.simprints.infra.config.store.models.FingerprintConfiguration
import com.simprints.infra.config.store.models.GeneralConfiguration
import com.simprints.infra.config.store.models.IdentificationConfiguration
import com.simprints.infra.config.store.models.MaxCaptureAttempts
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.models.ProjectState
import com.simprints.infra.config.store.models.SettingsPasswordConfig
import com.simprints.infra.config.store.models.SynchronizationConfiguration
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.config.store.models.UpSynchronizationConfiguration
import com.simprints.infra.config.store.models.Vero1Configuration
import com.simprints.infra.config.store.models.Vero2Configuration

internal val generalConfiguration = GeneralConfiguration(
    listOf(GeneralConfiguration.Modality.FACE),
    listOf(GeneralConfiguration.Modality.FACE),
    listOf("en"),
    "en",
    collectLocation = true,
    duplicateBiometricEnrolmentCheck = false,
    settingsPassword = SettingsPasswordConfig.NotSet,
)
internal val decisionPolicy = DecisionPolicy(10, 30, 40)

internal val vero2Configuration = Vero2Configuration(
    30,
    Vero2Configuration.ImageSavingStrategy.EAGER,
    Vero2Configuration.CaptureStrategy.SECUGEN_ISO_1000_DPI,
    Vero2Configuration.LedsMode.BASIC,
    mapOf("E-1" to Vero2Configuration.Vero2FirmwareVersions("1.1", "1.2", "1.4")),
)
internal val faceConfiguration =
    FaceConfiguration(
        allowedSDKs = listOf(FaceConfiguration.BioSdk.RANK_ONE),
        rankOne = FaceConfiguration.FaceSdkConfiguration(
            nbOfImagesToCapture = 2,
            qualityThreshold = -1f,
            imageSavingStrategy = FaceConfiguration.ImageSavingStrategy.NEVER,
            decisionPolicy = decisionPolicy,
            version = "1.0",
        ),
    )

internal val fingerprintConfiguration = FingerprintConfiguration(
    allowedScanners = listOf(FingerprintConfiguration.VeroGeneration.VERO_2),
    allowedSDKs = listOf(FingerprintConfiguration.BioSdk.SECUGEN_SIM_MATCHER),
    displayHandIcons = true,
    secugenSimMatcher = FingerprintConfiguration.FingerprintSdkConfiguration(
        listOf(Finger.LEFT_3RD_FINGER),
        decisionPolicy,
        FingerprintConfiguration.FingerComparisonStrategy.SAME_FINGER,
        vero1 = Vero1Configuration(10),
        vero2 = vero2Configuration,
        allowedAgeRange = AgeGroup(0, null),
        verificationMatchThreshold = 42.0f,
        maxCaptureAttempts = MaxCaptureAttempts(noFingerDetected = 17),
    ),
    nec = null,
)

internal val consentConfiguration = ConsentConfiguration(
    programName = "programName",
    organizationName = "organizationName",
    collectConsent = true,
    displaySimprintsLogo = false,
    allowParentalConsent = false,
    generalPrompt = ConsentConfiguration.ConsentPromptConfiguration(
        enrolmentVariant = ConsentConfiguration.ConsentEnrolmentVariant.STANDARD,
        dataSharedWithPartner = true,
        dataUsedForRAndD = false,
        privacyRights = true,
        confirmation = true,
    ),
    parentalPrompt = ConsentConfiguration.ConsentPromptConfiguration(
        enrolmentVariant = ConsentConfiguration.ConsentEnrolmentVariant.ENROLMENT_ONLY,
        dataSharedWithPartner = true,
        dataUsedForRAndD = false,
        privacyRights = false,
        confirmation = true,
    ),
)

internal val simprintsUpSyncConfigurationConfiguration = UpSynchronizationConfiguration.SimprintsUpSynchronizationConfiguration(
    UpSynchronizationConfiguration.UpSynchronizationKind.ALL,
    UpSynchronizationConfiguration.UpSyncBatchSizes.default(),
    false,
)

internal val synchronizationConfiguration = SynchronizationConfiguration(
    SynchronizationConfiguration.Frequency.PERIODICALLY,
    UpSynchronizationConfiguration(
        simprintsUpSyncConfigurationConfiguration,
        UpSynchronizationConfiguration.CoSyncUpSynchronizationConfiguration(
            UpSynchronizationConfiguration.UpSynchronizationKind.NONE,
        ),
    ),
    DownSynchronizationConfiguration(
        DownSynchronizationConfiguration.PartitionType.PROJECT,
        1,
        listOf("module1".asTokenizableEncrypted()),
        "PT24H",
    ),
)

internal val identificationConfiguration =
    IdentificationConfiguration(4, IdentificationConfiguration.PoolType.PROJECT)

internal val projectConfiguration = ProjectConfiguration(
    "id",
    "projectId",
    "updatedAt",
    generalConfiguration,
    faceConfiguration,
    fingerprintConfiguration,
    consentConfiguration,
    identificationConfiguration,
    synchronizationConfiguration,
    null,
)

internal const val TOKENIZATION_JSON =
    "{\"primaryKeyId\":12345,\"key\":[{\"keyData\":{\"typeUrl\":\"typeUrl\",\"value\":\"value\",\"keyMaterialType\":\"keyMaterialType\"},\"status\":\"enabled\",\"keyId\":123456789,\"outputPrefixType\":\"outputPrefixType\"}]}"

internal val tokenizationKeysDomain = mapOf(TokenKeyType.AttendantId to TOKENIZATION_JSON)

internal val project = Project(
    id = "id",
    name = "name",
    description = "description",
    state = ProjectState.RUNNING,
    creator = "creator",
    imageBucket = "url",
    baseUrl = "baseUrl",
    tokenizationKeys = tokenizationKeysDomain,
)
