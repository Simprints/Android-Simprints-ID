package com.simprints.infra.config.testtools

import com.simprints.infra.config.domain.models.*
import com.simprints.infra.config.remote.models.*

internal val apiConsentConfiguration = ApiConsentConfiguration(
    programName = "programName",
    organizationName = "organizationName",
    collectConsent = true,
    displaySimprintsLogo = false,
    allowParentalConsent = false,
    generalPrompt = ApiConsentConfiguration.ConsentPromptConfiguration(
        enrolmentVariant = ApiConsentConfiguration.ConsentEnrolmentVariant.STANDARD,
        dataSharedWithPartner = true,
        dataUsedForRAndD = false,
        privacyRights = true,
        confirmation = true,
    ),
    parentalPrompt = null,
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
    parentalPrompt = null,
)

internal val apiDecisionPolicy = ApiDecisionPolicy(10, 30, 40)
internal val decisionPolicy = DecisionPolicy(10, 30, 40)

internal val apiFaceConfiguration =
    ApiFaceConfiguration(2, -1, ApiFaceConfiguration.ImageSavingStrategy.NEVER, apiDecisionPolicy)
internal val faceConfiguration =
    FaceConfiguration(2, -1, FaceConfiguration.ImageSavingStrategy.NEVER, decisionPolicy)

internal val apiVero2Configuration = ApiVero2Configuration(
    ApiVero2Configuration.ImageSavingStrategy.EAGER,
    ApiVero2Configuration.CaptureStrategy.SECUGEN_ISO_1000_DPI,
    false,
    mapOf("E-1" to ApiVero2Configuration.ApiVero2FirmwareVersions("1.1", "1.2", "1.4"))
)

internal val vero2Configuration = Vero2Configuration(
    Vero2Configuration.ImageSavingStrategy.EAGER,
    Vero2Configuration.CaptureStrategy.SECUGEN_ISO_1000_DPI,
    false,
    mapOf("E-1" to Vero2Configuration.Vero2FirmwareVersions("1.1", "1.2", "1.4"))
)

internal val apiFingerprintConfiguration = ApiFingerprintConfiguration(
    listOf(ApiFingerprintConfiguration.Finger.LEFT_3RD_FINGER),
    10,
    apiDecisionPolicy,
    listOf(ApiFingerprintConfiguration.VeroGeneration.VERO_2),
    ApiFingerprintConfiguration.FingerComparisonStrategy.SAME_FINGER,
    true,
    apiVero2Configuration,
)

internal val fingerprintConfiguration = FingerprintConfiguration(
    listOf(FingerprintConfiguration.Finger.LEFT_3RD_FINGER),
    10,
    decisionPolicy,
    listOf(FingerprintConfiguration.VeroGeneration.VERO_2),
    FingerprintConfiguration.FingerComparisonStrategy.SAME_FINGER,
    true,
    vero2Configuration,
)

internal val apiGeneralConfiguration = ApiGeneralConfiguration(
    listOf(ApiGeneralConfiguration.Modality.FACE),
    listOf("en"),
    "en",
    collectLocation = true,
    duplicateBiometricEnrolmentCheck = false,
)

internal val generalConfiguration = GeneralConfiguration(
    listOf(GeneralConfiguration.Modality.FACE),
    listOf("en"),
    "en",
    collectLocation = true,
    duplicateBiometricEnrolmentCheck = false,
)

internal val apiIdentificationConfiguration =
    ApiIdentificationConfiguration(4, ApiIdentificationConfiguration.PoolType.PROJECT)

internal val identificationConfiguration =
    IdentificationConfiguration(4, IdentificationConfiguration.PoolType.PROJECT)

internal val apiSynchronizationConfiguration = ApiSynchronizationConfiguration(
    ApiSynchronizationConfiguration.Frequency.PERIODICALLY,
    ApiSynchronizationConfiguration.ApiUpSynchronizationConfiguration(
        ApiSynchronizationConfiguration.ApiUpSynchronizationConfiguration.ApiSimprintsUpSynchronizationConfiguration(
            ApiSynchronizationConfiguration.ApiUpSynchronizationConfiguration.UpSynchronizationKind.ALL
        ),
        ApiSynchronizationConfiguration.ApiUpSynchronizationConfiguration.ApiCoSyncUpSynchronizationConfiguration(
            ApiSynchronizationConfiguration.ApiUpSynchronizationConfiguration.UpSynchronizationKind.ALL
        )
    ),
    ApiSynchronizationConfiguration.ApiDownSynchronizationConfiguration(
        ApiSynchronizationConfiguration.ApiDownSynchronizationConfiguration.PartitionType.PROJECT,
        1,
        listOf("module1")
    )
)

internal val synchronizationConfiguration = SynchronizationConfiguration(
    SynchronizationConfiguration.Frequency.PERIODICALLY,
    UpSynchronizationConfiguration(
        UpSynchronizationConfiguration.SimprintsUpSynchronizationConfiguration(
            UpSynchronizationConfiguration.UpSynchronizationKind.ALL
        ),
        UpSynchronizationConfiguration.CoSyncUpSynchronizationConfiguration(
            UpSynchronizationConfiguration.UpSynchronizationKind.ALL
        )
    ),
    DownSynchronizationConfiguration(
        DownSynchronizationConfiguration.PartitionType.PROJECT,
        1,
        listOf("module1")
    )
)

internal val apiProjectConfiguration = ApiProjectConfiguration(
    "projectId",
    apiGeneralConfiguration,
    apiFaceConfiguration,
    apiFingerprintConfiguration,
    apiConsentConfiguration,
    apiIdentificationConfiguration,
    apiSynchronizationConfiguration
)

internal val projectConfiguration = ProjectConfiguration(
    "projectId",
    generalConfiguration,
    faceConfiguration,
    fingerprintConfiguration,
    consentConfiguration,
    identificationConfiguration,
    synchronizationConfiguration
)

internal val apiProject = ApiProject("id", "name", "description", "creator", "url")
internal val project = Project("id", "name", "description", "creator", "url")
