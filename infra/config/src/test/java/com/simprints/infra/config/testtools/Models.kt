package com.simprints.infra.config.testtools

import com.simprints.core.tools.extentions.singleItemList
import com.simprints.infra.config.domain.models.ConsentConfiguration
import com.simprints.infra.config.domain.models.DecisionPolicy
import com.simprints.infra.config.domain.models.DeviceConfiguration
import com.simprints.infra.config.domain.models.DownSynchronizationConfiguration
import com.simprints.infra.config.domain.models.FaceConfiguration
import com.simprints.infra.config.domain.models.Finger
import com.simprints.infra.config.domain.models.FingerprintConfiguration
import com.simprints.infra.config.domain.models.GeneralConfiguration
import com.simprints.infra.config.domain.models.IdentificationConfiguration
import com.simprints.infra.config.domain.models.KeyMaterialType
import com.simprints.infra.config.domain.models.Project
import com.simprints.infra.config.domain.models.ProjectConfiguration
import com.simprints.infra.config.domain.models.SettingsPasswordConfig
import com.simprints.infra.config.domain.models.SynchronizationConfiguration
import com.simprints.infra.config.domain.models.TokenKeyType
import com.simprints.infra.config.domain.models.TokenizationKeyData
import com.simprints.infra.config.domain.models.UpSynchronizationConfiguration
import com.simprints.infra.config.domain.models.Vero1Configuration
import com.simprints.infra.config.domain.models.Vero2Configuration
import com.simprints.infra.config.local.models.KeyData
import com.simprints.infra.config.local.models.ProtoConsentConfiguration
import com.simprints.infra.config.local.models.ProtoDecisionPolicy
import com.simprints.infra.config.local.models.ProtoDeviceConfiguration
import com.simprints.infra.config.local.models.ProtoDownSynchronizationConfiguration
import com.simprints.infra.config.local.models.ProtoFaceConfiguration
import com.simprints.infra.config.local.models.ProtoFinger
import com.simprints.infra.config.local.models.ProtoFingerprintConfiguration
import com.simprints.infra.config.local.models.ProtoGeneralConfiguration
import com.simprints.infra.config.local.models.ProtoIdentificationConfiguration
import com.simprints.infra.config.local.models.ProtoProject
import com.simprints.infra.config.local.models.ProtoProjectConfiguration
import com.simprints.infra.config.local.models.ProtoSynchronizationConfiguration
import com.simprints.infra.config.local.models.ProtoUpSynchronizationConfiguration
import com.simprints.infra.config.local.models.ProtoVero1Configuration
import com.simprints.infra.config.local.models.ProtoVero2Configuration
import com.simprints.infra.config.local.models.TokenizationItem
import com.simprints.infra.config.local.models.TokenizationKey
import com.simprints.infra.config.remote.models.ApiConsentConfiguration
import com.simprints.infra.config.remote.models.ApiDecisionPolicy
import com.simprints.infra.config.remote.models.ApiFaceConfiguration
import com.simprints.infra.config.remote.models.ApiFingerprintConfiguration
import com.simprints.infra.config.remote.models.ApiGeneralConfiguration
import com.simprints.infra.config.remote.models.ApiIdentificationConfiguration
import com.simprints.infra.config.remote.models.ApiProject
import com.simprints.infra.config.remote.models.ApiProjectConfiguration
import com.simprints.infra.config.remote.models.ApiSynchronizationConfiguration
import com.simprints.infra.config.remote.models.ApiVero1Configuration
import com.simprints.infra.config.remote.models.ApiVero2Configuration

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
    30,
    ApiVero2Configuration.ImageSavingStrategy.EAGER,
    ApiVero2Configuration.CaptureStrategy.SECUGEN_ISO_1000_DPI,
    false,
    mapOf("E-1" to ApiVero2Configuration.ApiVero2FirmwareVersions("1.1", "1.2", "1.4"))
)

internal val vero2Configuration = Vero2Configuration(
    30,
    Vero2Configuration.ImageSavingStrategy.EAGER,
    Vero2Configuration.CaptureStrategy.SECUGEN_ISO_1000_DPI,
    false,
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
    listOf(ApiFingerprintConfiguration.Finger.LEFT_3RD_FINGER),
    apiDecisionPolicy,
    listOf(ApiFingerprintConfiguration.VeroGeneration.VERO_2),
    ApiFingerprintConfiguration.FingerComparisonStrategy.SAME_FINGER,
    true,
    ApiVero1Configuration(10),
    apiVero2Configuration,
)

internal val fingerprintConfiguration = FingerprintConfiguration(
    listOf(Finger.LEFT_3RD_FINGER),
    decisionPolicy,
    listOf(FingerprintConfiguration.VeroGeneration.VERO_2),
    FingerprintConfiguration.FingerComparisonStrategy.SAME_FINGER,
    true,
    Vero1Configuration(10),
    vero2Configuration,
)

internal val protoFingerprintConfiguration = ProtoFingerprintConfiguration.newBuilder()
    .addFingersToCapture(ProtoFinger.LEFT_3RD_FINGER)
    .setDecisionPolicy(protoDecisionPolicy)
    .addAllowedVeroGenerations(ProtoFingerprintConfiguration.VeroGeneration.VERO_2)
    .setComparisonStrategyForVerification(ProtoFingerprintConfiguration.FingerComparisonStrategy.SAME_FINGER)
    .setDisplayHandIcons(true)
    .setVero1(ProtoVero1Configuration.newBuilder().setQualityThreshold(10).build())
    .setVero2(protoVero2Configuration)
    .build()

internal val apiGeneralConfiguration = ApiGeneralConfiguration(
    listOf(ApiGeneralConfiguration.Modality.FACE),
    listOf("en"),
    "en",
    collectLocation = true,
    duplicateBiometricEnrolmentCheck = false,
    settingsPassword = null,
)

internal val generalConfiguration = GeneralConfiguration(
    listOf(GeneralConfiguration.Modality.FACE),
    listOf("en"),
    "en",
    collectLocation = true,
    duplicateBiometricEnrolmentCheck = false,
    settingsPassword = SettingsPasswordConfig.NotSet,
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

internal val tokenizationKeyData = TokenizationKeyData(
    primaryKeyId = 123456789,
    typeUrl = "typeUrl",
    value = "value",
    keyMaterialType = KeyMaterialType.Symmetric,
    isEnabled = true,
    keyId = 12345,
    outputPrefixType = "outputPrefixType"
)

internal val tokenizationJson = with(tokenizationKeyData) {
    "{\"primaryKeyId\":$primaryKeyId,\"key\":[{\"keyData\":{\"typeUrl\":\"$typeUrl\",\"value\":\"$value\",\"keyMaterialType\":\"$keyMaterialType\"},\"status\":\"enabled\",\"keyId\":$keyId,\"outputPrefixType\":\"$outputPrefixType\"}]}"
}
internal val tokenizationKeys = mapOf(TokenKeyType.AttendantId.toString() to tokenizationJson)
internal val tokenizationKey = with(tokenizationKeyData) {
    TokenizationKey(
        keyData = KeyData(
            typeUrl = typeUrl,
            value = value,
            keyMaterialType = keyMaterialType.toString()
        ),
        status = "enabled",
        keyId = keyId,
        outputPrefixType = outputPrefixType
    )
}
internal val tokenizationItem = with(tokenizationKeyData) {
    TokenizationItem(
        primaryKeyId = primaryKeyId,
        key = tokenizationKey.singleItemList()
    )
}
internal val apiProject = ApiProject(
    id = "id",
    name = "name",
    description = "description",
    creator = "creator",
    imageBucket = "url",
    baseUrl = "baseUrl",
    tokenizationKeys = tokenizationKeys
)
internal val project = Project(
    id = "id",
    name = "name",
    description = "description",
    creator = "creator",
    imageBucket = "url",
    baseUrl = "baseUrl",
    tokenizationKeys = mapOf(TokenKeyType.AttendantId to tokenizationKeyData)
)
internal val protoProject = ProtoProject.newBuilder()
    .setId("id")
    .setName("name")
    .setDescription("description")
    .setCreator("creator")
    .setImageBucket("url")
    .setBaseUrl("baseUrl")
    .putAllTokenizationKeys(tokenizationKeys)
    .build()

internal val deviceConfiguration =
    DeviceConfiguration("en", listOf("module1", "module2"), "instruction")
internal val protoDeviceConfiguration = ProtoDeviceConfiguration.newBuilder()
    .setLanguage(
        ProtoDeviceConfiguration.Language.newBuilder().setLanguage("en").build()
    )
    .addAllModuleSelected(listOf("module1", "module2"))
    .setLastInstructionId("instruction")
    .build()
