package com.simprints.infra.config.testtools

import com.simprints.infra.config.domain.models.*
import com.simprints.infra.config.domain.models.Project
import com.simprints.infra.config.local.models.*
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
internal val protoConsentConfiguration = ProtoConsentConfiguration.newBuilder()
    .setProgramName("programName")
    .setOrganizationName("organizationName")
    .setCollectConsent(true)
    .setDisplaySimprintsLogo(false)
    .setAllowParentalConsent(false)
    .setGeneralPrompt(
        ProtoConsentConfiguration.ConsentPromptConfiguration.newBuilder()
            .setEnrolmentVariant(ProtoConsentConfiguration.ConsentEnrolmentVariant.STANDARD)
            .setDataSharedWithPartner(true)
            .setDataUsedForRAndD(false)
            .setPrivacyRights(true)
            .setConfirmation(true)
            .build()
    )
    .build()

internal val apiDecisionPolicy = ApiDecisionPolicy(10, 30, 40)
internal val decisionPolicy = DecisionPolicy(10, 30, 40)
internal val protoDecisionPolicy =
    ProtoDecisionPolicy.newBuilder().setLow(10).setMedium(30).setHigh(40).build()

internal val apiFaceConfiguration =
    ApiFaceConfiguration(2, -1, ApiFaceConfiguration.ImageSavingStrategy.NEVER, apiDecisionPolicy)
internal val faceConfiguration =
    FaceConfiguration(2, -1, FaceConfiguration.ImageSavingStrategy.NEVER, decisionPolicy)
internal val protoFaceConfiguration = ProtoFaceConfiguration.newBuilder()
    .setNbOfImagesToCapture(2)
    .setQualityThreshold(-1)
    .setImageSavingStrategy(ProtoFaceConfiguration.ImageSavingStrategy.NEVER)
    .setDecisionPolicy(protoDecisionPolicy)
    .build()

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

internal val protoVero2Configuration = ProtoVero2Configuration.newBuilder()
    .setCaptureStrategy(ProtoVero2Configuration.CaptureStrategy.SECUGEN_ISO_1000_DPI)
    .setImageSavingStrategy(ProtoVero2Configuration.ImageSavingStrategy.EAGER)
    .setDisplayLiveFeedback(false)
    .putAllFirmwareVersions(
        mapOf(
            "E-1" to ProtoVero2Configuration.Vero2FirmwareVersions.newBuilder().setCypress("1.1")
                .setStm("1.2").setUn20("1.4").build()
        )
    )
    .build()

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

internal val protoFingerprintConfiguration = ProtoFingerprintConfiguration.newBuilder()
    .addFingersToCapture(ProtoFingerprintConfiguration.Finger.LEFT_3RD_FINGER)
    .setQualityThreshold(10)
    .setDecisionPolicy(protoDecisionPolicy)
    .addAllowedVeroGenerations(ProtoFingerprintConfiguration.VeroGeneration.VERO_2)
    .setComparisonStrategyForVerification(ProtoFingerprintConfiguration.FingerComparisonStrategy.SAME_FINGER)
    .setDisplayHandIcons(true)
    .setVero2(protoVero2Configuration)
    .build()

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

internal val protoGeneralConfiguration = ProtoGeneralConfiguration.newBuilder()
    .addModalities(ProtoGeneralConfiguration.Modality.FACE)
    .addLanguageOptions("en")
    .setDefaultLanguage("en")
    .setCollectLocation(true)
    .setDuplicateBiometricEnrolmentCheck(false)
    .build()

internal val apiIdentificationConfiguration =
    ApiIdentificationConfiguration(4, ApiIdentificationConfiguration.PoolType.PROJECT)

internal val identificationConfiguration =
    IdentificationConfiguration(4, IdentificationConfiguration.PoolType.PROJECT)

internal val protoIdentificationConfiguration = ProtoIdentificationConfiguration.newBuilder()
    .setMaxNbOfReturnedCandidates(4).setPoolType(ProtoIdentificationConfiguration.PoolType.PROJECT)
    .build()

internal val apiSynchronizationConfiguration = ApiSynchronizationConfiguration(
    ApiSynchronizationConfiguration.Frequency.PERIODICALLY,
    ApiSynchronizationConfiguration.ApiUpSynchronizationConfiguration(
        ApiSynchronizationConfiguration.ApiUpSynchronizationConfiguration.ApiSimprintsUpSynchronizationConfiguration(
            ApiSynchronizationConfiguration.ApiUpSynchronizationConfiguration.UpSynchronizationKind.ALL
        ),
        ApiSynchronizationConfiguration.ApiUpSynchronizationConfiguration.ApiCoSyncUpSynchronizationConfiguration(
            ApiSynchronizationConfiguration.ApiUpSynchronizationConfiguration.UpSynchronizationKind.NONE
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
            UpSynchronizationConfiguration.UpSynchronizationKind.NONE
        )
    ),
    DownSynchronizationConfiguration(
        DownSynchronizationConfiguration.PartitionType.PROJECT,
        1,
        listOf("module1")
    )
)

internal val protoSynchronizationConfiguration = ProtoSynchronizationConfiguration.newBuilder()
    .setFrequency(ProtoSynchronizationConfiguration.Frequency.PERIODICALLY)
    .setUp(
        ProtoUpSynchronizationConfiguration.newBuilder()
            .setSimprints(
                ProtoUpSynchronizationConfiguration.SimprintsUpSynchronizationConfiguration.newBuilder()
                    .setKind(ProtoUpSynchronizationConfiguration.UpSynchronizationKind.ALL)
                    .build()
            )
            .setCoSync(
                ProtoUpSynchronizationConfiguration.CoSyncUpSynchronizationConfiguration.newBuilder()
                    .setKind(ProtoUpSynchronizationConfiguration.UpSynchronizationKind.NONE)
                    .build()
            )
            .build()
    )
    .setDown(
        ProtoDownSynchronizationConfiguration.newBuilder()
            .setPartitionType(ProtoDownSynchronizationConfiguration.PartitionType.PROJECT)
            .setMaxNbOfModules(1)
            .addModuleOptions("module1")
    )
    .build()

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

internal val protoProjectConfiguration = ProtoProjectConfiguration.newBuilder()
    .setProjectId("projectId")
    .setGeneral(protoGeneralConfiguration)
    .setFace(protoFaceConfiguration)
    .setFingerprint(protoFingerprintConfiguration)
    .setConsent(protoConsentConfiguration)
    .setIdentification(protoIdentificationConfiguration)
    .setSynchronization(protoSynchronizationConfiguration)
    .build()

internal val apiProject = ApiProject("id", "name", "description", "creator", "url")
internal val project = Project("id", "name", "description", "creator", "url")
internal val protoProject = ProtoProject.newBuilder()
    .setId("id")
    .setName("name")
    .setDescription("description")
    .setCreator("creator")
    .setImageBucket("url")
    .build()
