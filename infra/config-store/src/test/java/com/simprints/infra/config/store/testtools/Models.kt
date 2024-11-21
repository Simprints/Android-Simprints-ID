package com.simprints.infra.config.store.testtools

import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.infra.config.store.local.models.ProtoAllowedAgeRange
import com.simprints.infra.config.store.local.models.ProtoConsentConfiguration
import com.simprints.infra.config.store.local.models.ProtoDecisionPolicy
import com.simprints.infra.config.store.local.models.ProtoDeviceConfiguration
import com.simprints.infra.config.store.local.models.ProtoDownSynchronizationConfiguration
import com.simprints.infra.config.store.local.models.ProtoFaceConfiguration
import com.simprints.infra.config.store.local.models.ProtoFinger
import com.simprints.infra.config.store.local.models.ProtoFingerprintConfiguration
import com.simprints.infra.config.store.local.models.ProtoFingerprintConfiguration.ProtoMaxCaptureAttempts
import com.simprints.infra.config.store.local.models.ProtoGeneralConfiguration
import com.simprints.infra.config.store.local.models.ProtoIdentificationConfiguration
import com.simprints.infra.config.store.local.models.ProtoProject
import com.simprints.infra.config.store.local.models.ProtoProjectConfiguration
import com.simprints.infra.config.store.local.models.ProtoSynchronizationConfiguration
import com.simprints.infra.config.store.local.models.ProtoUpSyncBatchSizes
import com.simprints.infra.config.store.local.models.ProtoUpSynchronizationConfiguration
import com.simprints.infra.config.store.local.models.ProtoVero1Configuration
import com.simprints.infra.config.store.local.models.ProtoVero2Configuration
import com.simprints.infra.config.store.models.*
import com.simprints.infra.config.store.remote.models.*
import com.simprints.infra.config.store.models.AgeGroup
import com.simprints.infra.config.store.models.FaceConfiguration.FaceSdkConfiguration
import com.simprints.infra.config.store.remote.models.ApiConsentConfiguration
import com.simprints.infra.config.store.remote.models.ApiDecisionPolicy
import com.simprints.infra.config.store.remote.models.ApiFaceConfiguration
import com.simprints.infra.config.store.remote.models.ApiFaceConfiguration.ApiFaceSdkConfiguration
import com.simprints.infra.config.store.remote.models.ApiFingerprintConfiguration
import com.simprints.infra.config.store.remote.models.ApiGeneralConfiguration
import com.simprints.infra.config.store.remote.models.ApiIdentificationConfiguration
import com.simprints.infra.config.store.remote.models.ApiProject
import com.simprints.infra.config.store.remote.models.ApiProjectConfiguration
import com.simprints.infra.config.store.remote.models.ApiSynchronizationConfiguration
import com.simprints.infra.config.store.remote.models.ApiVero1Configuration
import com.simprints.infra.config.store.remote.models.ApiVero2Configuration

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
    parentalPrompt = ApiConsentConfiguration.ConsentPromptConfiguration(
        enrolmentVariant = ApiConsentConfiguration.ConsentEnrolmentVariant.ENROLMENT_ONLY,
        dataSharedWithPartner = true,
        dataUsedForRAndD = false,
        privacyRights = false,
        confirmation = true,
    ),
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
    .setParentalPrompt(
        ProtoConsentConfiguration.ConsentPromptConfiguration.newBuilder()
            .setEnrolmentVariant(ProtoConsentConfiguration.ConsentEnrolmentVariant.ENROLMENT_ONLY)
            .setDataSharedWithPartner(true)
            .setDataUsedForRAndD(false)
            .setPrivacyRights(false)
            .setConfirmation(true)
            .build()
    )
    .build()

internal val apiAllowedAgeRange = ApiAllowedAgeRange(2, 10)
internal val apiMaxCaptureAttempts = ApiMaxCaptureAttempts(noFingerDetected = 17)
internal val allowedAgeRange = AgeGroup(2, 10)
internal val protoAllowedAgeRange = ProtoAllowedAgeRange.newBuilder().setStartInclusive(2).setEndExclusive(10).build()

internal val apiDecisionPolicy = ApiDecisionPolicy(10, 30, 40)
internal val decisionPolicy = DecisionPolicy(10, 30, 40)
internal val protoDecisionPolicy =
    ProtoDecisionPolicy.newBuilder().setLow(10).setMedium(30).setHigh(40).build()
internal val rankOneConfiguration = FaceSdkConfiguration(
    nbOfImagesToCapture = 2,
    qualityThreshold = -1f,
    imageSavingStrategy = FaceConfiguration.ImageSavingStrategy.NEVER,
    decisionPolicy = decisionPolicy,
    allowedAgeRange = AgeGroup(0, null),
    verificationMatchThreshold = null,
    version = "1.0"
)

internal val apiFaceConfiguration = ApiFaceConfiguration(
        allowedSDKs = listOf(ApiFaceConfiguration.BioSdk.RANK_ONE),
        rankOne = ApiFaceSdkConfiguration(
            nbOfImagesToCapture = 2,
            qualityThreshold = -1f,
            decisionPolicy = apiDecisionPolicy,
            imageSavingStrategy = ApiFaceConfiguration.ImageSavingStrategy.NEVER,
            allowedAgeRange = null,
            verificationMatchThreshold = null,
            version = "1.0"
        )
    )
internal val faceConfiguration = FaceConfiguration(
    allowedSDKs = listOf(FaceConfiguration.BioSdk.RANK_ONE),
    rankOne = rankOneConfiguration
)
internal val protoFaceConfiguration = ProtoFaceConfiguration.newBuilder()
    .addAllowedSdks(ProtoFaceConfiguration.ProtoBioSdk.RANK_ONE)
    .setRankOne(
        ProtoFaceConfiguration.ProtoFaceSdkConfiguration.newBuilder()
            .setNbOfImagesToCapture(2)
            .setQualityThresholdPrecise(-1f)
            .setImageSavingStrategy(ProtoFaceConfiguration.ImageSavingStrategy.NEVER)
            .setDecisionPolicy(protoDecisionPolicy)
            .setVersion("1.0")
            .setAllowedAgeRange(ProtoAllowedAgeRange.newBuilder().build())
            .build()
    )
    .build()

internal val apiVero2Configuration = ApiVero2Configuration(
    30,
    ApiVero2Configuration.ImageSavingStrategy.EAGER,
    ApiVero2Configuration.CaptureStrategy.SECUGEN_ISO_1000_DPI,
    ApiVero2Configuration.LedsMode.BASIC,
    mapOf("E-1" to ApiVero2Configuration.ApiVero2FirmwareVersions("1.1", "1.2", "1.4"))
)

internal val vero2Configuration = Vero2Configuration(
    30,
    Vero2Configuration.ImageSavingStrategy.EAGER,
    Vero2Configuration.CaptureStrategy.SECUGEN_ISO_1000_DPI,
    Vero2Configuration.LedsMode.BASIC,
    mapOf("E-1" to Vero2Configuration.Vero2FirmwareVersions("1.1", "1.2", "1.4"))
)

internal val protoVero2Configuration = ProtoVero2Configuration.newBuilder()
    .setQualityThreshold(30)
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
    allowedScanners = listOf(ApiFingerprintConfiguration.VeroGeneration.VERO_2),
    allowedSDKs = listOf(ApiFingerprintConfiguration.BioSdk.SECUGEN_SIM_MATCHER),
    displayHandIcons = true,
    secugenSimMatcher = ApiFingerprintConfiguration.ApiFingerprintSdkConfiguration(
        fingersToCapture = listOf(ApiFingerprintConfiguration.Finger.LEFT_3RD_FINGER),
        decisionPolicy = apiDecisionPolicy,
        comparisonStrategyForVerification = ApiFingerprintConfiguration.FingerComparisonStrategy.SAME_FINGER,
        vero1 = ApiVero1Configuration(10),
        vero2 = apiVero2Configuration,
        allowedAgeRange = apiAllowedAgeRange,
        verificationMatchThreshold = 42.0f,
        maxCaptureAttempts = apiMaxCaptureAttempts
    ),
    nec = null,
)

internal val fingerprintConfiguration = FingerprintConfiguration(
    allowedScanners = listOf(FingerprintConfiguration.VeroGeneration.VERO_2),
    allowedSDKs = listOf(FingerprintConfiguration.BioSdk.SECUGEN_SIM_MATCHER),
    displayHandIcons = true,
    secugenSimMatcher = FingerprintConfiguration.FingerprintSdkConfiguration(
        fingersToCapture = listOf(Finger.LEFT_3RD_FINGER),
        decisionPolicy = decisionPolicy,
        comparisonStrategyForVerification = FingerprintConfiguration.FingerComparisonStrategy.SAME_FINGER,
        vero1 = Vero1Configuration(qualityThreshold = 10),
        vero2 = vero2Configuration,
        allowedAgeRange = allowedAgeRange,
        verificationMatchThreshold = 42.0f,
        maxCaptureAttempts = MaxCaptureAttempts(noFingerDetected = 17)
    ),
    nec = null,
)

internal val protoFingerprintConfiguration = ProtoFingerprintConfiguration.newBuilder()
    .addAllowedScanners(ProtoFingerprintConfiguration.VeroGeneration.VERO_2)
    .addAllowedSdks(ProtoFingerprintConfiguration.ProtoBioSdk.SECUGEN_SIM_MATCHER)
    .setDisplayHandIcons(true)
    .setSecugenSimMatcher(
        ProtoFingerprintConfiguration.ProtoFingerprintSdkConfiguration.newBuilder()
            .addFingersToCapture(ProtoFinger.LEFT_3RD_FINGER)
            .setDecisionPolicy(protoDecisionPolicy)
            .setComparisonStrategyForVerification(ProtoFingerprintConfiguration.FingerComparisonStrategy.SAME_FINGER)
            .setVero1(ProtoVero1Configuration.newBuilder().setQualityThreshold(10).build())
            .setVero2(protoVero2Configuration)
            .setAllowedAgeRange(protoAllowedAgeRange)
            .setVerificationMatchThreshold(42.0f)
            .setMaxCaptureAttempts(ProtoMaxCaptureAttempts.newBuilder().setNoFingerDetected(17))
            .build()
    )
    .build()

internal val apiGeneralConfiguration = ApiGeneralConfiguration(
    listOf(ApiGeneralConfiguration.Modality.FACE),
    listOf(ApiGeneralConfiguration.Modality.FACE),
    listOf("en"),
    "en",
    collectLocation = true,
    duplicateBiometricEnrolmentCheck = false,
    settingsPassword = null,
)

internal val generalConfiguration = GeneralConfiguration(
    listOf(GeneralConfiguration.Modality.FACE),
    listOf(GeneralConfiguration.Modality.FACE),
    listOf("en"),
    "en",
    collectLocation = true,
    duplicateBiometricEnrolmentCheck = false,
    settingsPassword = SettingsPasswordConfig.NotSet,
)

internal val protoGeneralConfiguration = ProtoGeneralConfiguration.newBuilder()
    .addModalities(ProtoGeneralConfiguration.Modality.FACE)
    .addMatchingModalities(ProtoGeneralConfiguration.Modality.FACE)
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
            ApiSynchronizationConfiguration.ApiUpSynchronizationConfiguration.UpSynchronizationKind.ALL,
            ApiSynchronizationConfiguration.ApiUpSynchronizationConfiguration.ApiUpSyncBatchSizes(1, 2, 3),
            false,
        ),
        ApiSynchronizationConfiguration.ApiUpSynchronizationConfiguration.ApiCoSyncUpSynchronizationConfiguration(
            ApiSynchronizationConfiguration.ApiUpSynchronizationConfiguration.UpSynchronizationKind.NONE
        ),
    ),
    ApiSynchronizationConfiguration.ApiDownSynchronizationConfiguration(
        ApiSynchronizationConfiguration.ApiDownSynchronizationConfiguration.PartitionType.PROJECT,
        1,
        listOf("module1"),
        "PT24H",
    )
)

internal val simprintsUpSyncConfigurationConfiguration = UpSynchronizationConfiguration.SimprintsUpSynchronizationConfiguration(
    UpSynchronizationConfiguration.UpSynchronizationKind.ALL,
    UpSynchronizationConfiguration.UpSyncBatchSizes(1, 2, 3),
    false
)

internal val synchronizationConfiguration = SynchronizationConfiguration(
    SynchronizationConfiguration.Frequency.PERIODICALLY,
    UpSynchronizationConfiguration(
        simprintsUpSyncConfigurationConfiguration,
        UpSynchronizationConfiguration.CoSyncUpSynchronizationConfiguration(
            UpSynchronizationConfiguration.UpSynchronizationKind.NONE
        ),
    ),
    DownSynchronizationConfiguration(
        DownSynchronizationConfiguration.PartitionType.PROJECT,
        1,
        listOf("module1".asTokenizableEncrypted()),
        "PT24H",
    )
)

internal val protoSynchronizationConfiguration = ProtoSynchronizationConfiguration.newBuilder()
    .setFrequency(ProtoSynchronizationConfiguration.Frequency.PERIODICALLY)
    .setUp(
        ProtoUpSynchronizationConfiguration.newBuilder()
            .setSimprints(
                ProtoUpSynchronizationConfiguration.SimprintsUpSynchronizationConfiguration.newBuilder()
                    .setKind(ProtoUpSynchronizationConfiguration.UpSynchronizationKind.ALL)
                    .setBatchSizes(
                        ProtoUpSyncBatchSizes.newBuilder()
                            .setSessions(1)
                            .setUpSyncs(2)
                            .setDownSyncs(3)
                            .build()
                    )
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
            .setIsTokenized(true)
            .addModuleOptions("module1")
            .setMaxAge("PT24H")
            .build()
    )
    .build()

internal val customKeyMap: Map<String, Any>? = mapOf(
    "key1" to 7,
    "key2" to 4.2,
    "key3" to false,
    "key4" to "test"
)
internal const val protoCustomKeyMapJson = "{\"key1\":7,\"key2\":4.2,\"key3\":false,\"key4\":\"test\"}"

internal val apiProjectConfiguration = ApiProjectConfiguration(
    "id",
    "projectId",
    "updatedAt",
    apiGeneralConfiguration,
    apiFaceConfiguration,
    apiFingerprintConfiguration,
    apiConsentConfiguration,
    apiIdentificationConfiguration,
    apiSynchronizationConfiguration,
    customKeyMap,
)

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
    customKeyMap,
)

internal val protoProjectConfiguration = ProtoProjectConfiguration.newBuilder()
    .setId("id")
    .setProjectId("projectId")
    .setUpdatedAt("updatedAt")
    .setGeneral(protoGeneralConfiguration)
    .setFace(protoFaceConfiguration)
    .setFingerprint(protoFingerprintConfiguration)
    .setConsent(protoConsentConfiguration)
    .setIdentification(protoIdentificationConfiguration)
    .setSynchronization(protoSynchronizationConfiguration)
    .setCustomJson(protoCustomKeyMapJson)
    .build()


internal const val tokenizationJson =
    "{\"primaryKeyId\":12345,\"key\":[{\"keyData\":{\"typeUrl\":\"typeUrl\",\"value\":\"value\",\"keyMaterialType\":\"keyMaterialType\"},\"status\":\"enabled\",\"keyId\":123456789,\"outputPrefixType\":\"outputPrefixType\"}]}"

internal val tokenizationKeysDomain = mapOf(TokenKeyType.AttendantId to tokenizationJson)
internal val tokenizationKeysLocal = tokenizationKeysDomain.mapKeys {
    it.key.toString()
}

internal val apiProject = ApiProject(
    id = "id",
    name = "name",
    state = ApiProjectState.RUNNING,
    description = "description",
    creator = "creator",
    imageBucket = "url",
    baseUrl = "baseUrl",
    configuration = apiProjectConfiguration,
    tokenizationKeys = tokenizationKeysLocal
)
internal val project = Project(
    id = "id",
    name = "name",
    description = "description",
    state = ProjectState.RUNNING,
    creator = "creator",
    imageBucket = "url",
    baseUrl = "baseUrl",
    tokenizationKeys = tokenizationKeysDomain
)
internal val protoProject = ProtoProject.newBuilder()
    .setId("id")
    .setName("name")
    .setDescription("description")
    .setState("RUNNING")
    .setCreator("creator")
    .setImageBucket("url")
    .setBaseUrl("baseUrl")
    .putAllTokenizationKeys(tokenizationKeysLocal)
    .build()

internal val deviceConfiguration =
    DeviceConfiguration(
        "en",
        listOf("module1".asTokenizableEncrypted(), "module2".asTokenizableEncrypted()),
        "instruction"
    )
internal val protoDeviceConfiguration = ProtoDeviceConfiguration.newBuilder()
    .setLanguage(
        ProtoDeviceConfiguration.Language.newBuilder().setLanguage("en").build()
    )
    .setIsTokenized(true)
    .addAllModuleSelected(listOf("module1", "module2"))
    .setLastInstructionId("instruction")
    .build()

internal val apiDeviceState = ApiDeviceState(
    "deviceId",
    false,
    null
)
internal val deviceState = DeviceState(
    "deviceId",
    false,
    null
)
