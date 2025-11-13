package com.simprints.infra.sync.config.testtools

import com.simprints.core.domain.common.Modality
import com.simprints.core.domain.externalcredential.ExternalCredentialType
import com.simprints.core.domain.sample.SampleIdentifier
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.infra.config.store.models.AgeGroup
import com.simprints.infra.config.store.models.ConsentConfiguration
import com.simprints.infra.config.store.models.DecisionPolicy
import com.simprints.infra.config.store.models.DownSynchronizationConfiguration
import com.simprints.infra.config.store.models.FaceConfiguration
import com.simprints.infra.config.store.models.FingerprintConfiguration
import com.simprints.infra.config.store.models.Frequency
import com.simprints.infra.config.store.models.GeneralConfiguration
import com.simprints.infra.config.store.models.IdentificationConfiguration
import com.simprints.infra.config.store.models.MaxCaptureAttempts
import com.simprints.infra.config.store.models.MultiFactorIdConfiguration
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.models.ProjectState
import com.simprints.infra.config.store.models.SampleSynchronizationConfiguration
import com.simprints.infra.config.store.models.SettingsPasswordConfig
import com.simprints.infra.config.store.models.SynchronizationConfiguration
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.config.store.models.UpSynchronizationConfiguration
import com.simprints.infra.config.store.models.Vero1Configuration
import com.simprints.infra.config.store.models.Vero2Configuration

internal val generalConfiguration = GeneralConfiguration(
    listOf(Modality.FACE),
    listOf(Modality.FACE),
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
        simFace = FaceConfiguration.FaceSdkConfiguration(
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
        listOf(SampleIdentifier.LEFT_3RD_FINGER),
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
    kind = UpSynchronizationConfiguration.UpSynchronizationKind.ALL,
    batchSizes = UpSynchronizationConfiguration.UpSyncBatchSizes.default(),
    imagesRequireUnmeteredConnection = false,
    frequency = Frequency.PERIODICALLY,
)

internal val simprintsDownSyncConfigurationConfiguration = DownSynchronizationConfiguration.SimprintsDownSynchronizationConfiguration(
    partitionType = DownSynchronizationConfiguration.PartitionType.PROJECT,
    maxNbOfModules = 1,
    moduleOptions = listOf("module1".asTokenizableEncrypted()),
    maxAge = "PT24H",
    frequency = Frequency.PERIODICALLY,
)

internal val allowedExternalCredential = ExternalCredentialType.NHISCard

internal val multiFactorIdConfiguration = MultiFactorIdConfiguration(
    allowedExternalCredentials = listOf(allowedExternalCredential),
)

internal val synchronizationConfiguration = SynchronizationConfiguration(
    up = UpSynchronizationConfiguration(
        simprints = simprintsUpSyncConfigurationConfiguration,
        coSync = UpSynchronizationConfiguration.CoSyncUpSynchronizationConfiguration(
            UpSynchronizationConfiguration.UpSynchronizationKind.NONE,
        ),
    ),
    down = DownSynchronizationConfiguration(
        simprints = simprintsDownSyncConfigurationConfiguration,
        commCare = null,
    ),
    samples = SampleSynchronizationConfiguration(3),
)

internal val identificationConfiguration =
    IdentificationConfiguration(4, IdentificationConfiguration.PoolType.PROJECT)

internal val projectConfiguration = ProjectConfiguration(
    id = "id",
    projectId = "projectId",
    updatedAt = "updatedAt",
    general = generalConfiguration,
    face = faceConfiguration,
    fingerprint = fingerprintConfiguration,
    consent = consentConfiguration,
    identification = identificationConfiguration,
    synchronization = synchronizationConfiguration,
    multifactorId = multiFactorIdConfiguration,
    custom = null,
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
