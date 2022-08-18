package com.simprints.infra.config.local.migrations

import android.content.Context
import android.content.SharedPreferences
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.local.models.*
import com.simprints.infra.config.testtools.protoProjectConfiguration
import com.simprints.infra.login.LoginManager
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProjectConfigSharedPrefsMigrationTest {

    private val ctx = mockk<Context>()
    private val preferences = mockk<SharedPreferences>()
    private val loginManager = mockk<LoginManager>()
    private lateinit var projectConfigSharedPrefsMigration: ProjectConfigSharedPrefsMigration

    @Before
    fun setup() {
        every { ctx.getSharedPreferences(any(), any()) } returns preferences
        projectConfigSharedPrefsMigration = ProjectConfigSharedPrefsMigration(ctx, loginManager)
    }

    @Test
    fun `shouldMigrate should return true only if the project is signed in and the current data empty`() =
        runTest {
            every { loginManager.signedInProjectId } returns "project_id"

            val shouldMigrate =
                projectConfigSharedPrefsMigration.shouldMigrate(ProtoProjectConfiguration.getDefaultInstance())
            assertThat(shouldMigrate).isTrue()
        }

    @Test
    fun `shouldMigrate should return false if the project is not signed in`() =
        runTest {
            every { loginManager.signedInProjectId } returns ""

            val shouldMigrate =
                projectConfigSharedPrefsMigration.shouldMigrate(ProtoProjectConfiguration.getDefaultInstance())
            assertThat(shouldMigrate).isFalse()
        }

    @Test
    fun `shouldMigrate should return false if the current data is not empty`() =
        runTest {
            every { loginManager.signedInProjectId } returns "project_id"

            val shouldMigrate = projectConfigSharedPrefsMigration.shouldMigrate(
                protoProjectConfiguration
            )
            assertThat(shouldMigrate).isFalse()
        }

    @Test
    fun `should not migrate the data if the shared preferences returns an empty value`() = runTest {
        every { preferences.getString(any(), any()) } returns ""
        val proto = projectConfigSharedPrefsMigration.migrate(protoProjectConfiguration)
        assertThat(proto).isEqualTo(protoProjectConfiguration)
    }

    @Test
    fun `migrate should work when both face and fingerprint are missing`() = runTest {
        val json = concatMapsAsString(
            JSON_GENERAL_CONFIGURATION,
            JSON_CONSENT_CONFIGURATION,
            JSON_IDENTIFICATION_CONFIGURATION,
            JSON_SYNCHRONIZATION_CONFIGURATION
        )
        every { preferences.getString(any(), any()) } returns json
        every { loginManager.signedInProjectId } returns PROJECT_ID

        val expectedProto = ProtoProjectConfiguration.newBuilder()
            .setProjectId(PROJECT_ID)
            .setConsent(PROTO_CONSENT_CONFIGURATION)
            .setGeneral(PROTO_GENERAL_CONFIGURATION)
            .setIdentification(PROTO_IDENTIFICATION_CONFIGURATION)
            .setSynchronization(PROTO_SYNCHRONIZATION_CONFIGURATION)
            .build()

        val proto = projectConfigSharedPrefsMigration.migrate(protoProjectConfiguration)
        assertThat(proto).isEqualTo(expectedProto)
    }

    @Test
    fun `migrate should work when face is present`() = runTest {
        val json = concatMapsAsString(
            JSON_GENERAL_CONFIGURATION,
            JSON_CONSENT_CONFIGURATION,
            JSON_IDENTIFICATION_CONFIGURATION,
            JSON_SYNCHRONIZATION_CONFIGURATION,
            JSON_FACE_CONFIGURATION
        )
        every { preferences.getString(any(), any()) } returns json
        every { loginManager.signedInProjectId } returns PROJECT_ID

        val expectedProto = ProtoProjectConfiguration.newBuilder()
            .setProjectId(PROJECT_ID)
            .setConsent(PROTO_CONSENT_CONFIGURATION)
            .setGeneral(PROTO_GENERAL_CONFIGURATION)
            .setIdentification(PROTO_IDENTIFICATION_CONFIGURATION)
            .setSynchronization(PROTO_SYNCHRONIZATION_CONFIGURATION)
            .setFace(PROTO_FACE_CONFIGURATION)
            .build()

        val proto = projectConfigSharedPrefsMigration.migrate(protoProjectConfiguration)
        assertThat(proto).isEqualTo(expectedProto)
    }

    @Test
    fun `migrate should work when fingerprint is present without the vero2`() = runTest {
        val json = concatMapsAsString(
            JSON_GENERAL_CONFIGURATION,
            JSON_CONSENT_CONFIGURATION,
            JSON_IDENTIFICATION_CONFIGURATION,
            JSON_SYNCHRONIZATION_CONFIGURATION,
            JSON_FINGERPRINT_CONFIGURATION
        )
        every { preferences.getString(any(), any()) } returns json
        every { loginManager.signedInProjectId } returns PROJECT_ID

        val expectedProto = ProtoProjectConfiguration.newBuilder()
            .setProjectId(PROJECT_ID)
            .setConsent(PROTO_CONSENT_CONFIGURATION)
            .setGeneral(PROTO_GENERAL_CONFIGURATION)
            .setIdentification(PROTO_IDENTIFICATION_CONFIGURATION)
            .setSynchronization(PROTO_SYNCHRONIZATION_CONFIGURATION)
            .setFingerprint(PROTO_FINGERPRINT_CONFIGURATION)
            .build()

        val proto = projectConfigSharedPrefsMigration.migrate(protoProjectConfiguration)
        assertThat(proto).isEqualTo(expectedProto)
    }

    @Test
    fun `migrate should work when fingerprint is present with the vero2`() = runTest {
        val json = concatMapsAsString(
            JSON_GENERAL_CONFIGURATION,
            JSON_CONSENT_CONFIGURATION,
            JSON_IDENTIFICATION_CONFIGURATION,
            JSON_SYNCHRONIZATION_CONFIGURATION,
            JSON_FINGERPRINT_CONFIGURATION,
            JSON_VERO_2_CONFIGURATION
        )
        every { preferences.getString(any(), any()) } returns json
        every { loginManager.signedInProjectId } returns PROJECT_ID

        val expectedProto = ProtoProjectConfiguration.newBuilder()
            .setProjectId(PROJECT_ID)
            .setConsent(PROTO_CONSENT_CONFIGURATION)
            .setGeneral(PROTO_GENERAL_CONFIGURATION)
            .setIdentification(PROTO_IDENTIFICATION_CONFIGURATION)
            .setSynchronization(PROTO_SYNCHRONIZATION_CONFIGURATION)
            .setFingerprint(
                PROTO_FINGERPRINT_CONFIGURATION.toBuilder().setVero2(
                    PROTO_VERO_2_CONFIGURATION
                ).build()
            )
            .build()

        val proto = projectConfigSharedPrefsMigration.migrate(protoProjectConfiguration)
        assertThat(proto).isEqualTo(expectedProto)
    }

    @Test
    fun `migrate should work when fingerprint and face are present`() = runTest {
        val json = concatMapsAsString(
            JSON_GENERAL_CONFIGURATION,
            JSON_CONSENT_CONFIGURATION,
            JSON_IDENTIFICATION_CONFIGURATION,
            JSON_SYNCHRONIZATION_CONFIGURATION,
            JSON_FACE_CONFIGURATION,
            JSON_FINGERPRINT_CONFIGURATION,
            JSON_VERO_2_CONFIGURATION
        )
        every { preferences.getString(any(), any()) } returns json
        every { loginManager.signedInProjectId } returns PROJECT_ID

        val expectedProto = ProtoProjectConfiguration.newBuilder()
            .setProjectId(PROJECT_ID)
            .setConsent(PROTO_CONSENT_CONFIGURATION)
            .setGeneral(PROTO_GENERAL_CONFIGURATION)
            .setIdentification(PROTO_IDENTIFICATION_CONFIGURATION)
            .setSynchronization(PROTO_SYNCHRONIZATION_CONFIGURATION)
            .setFace(PROTO_FACE_CONFIGURATION)
            .setFingerprint(
                PROTO_FINGERPRINT_CONFIGURATION.toBuilder().setVero2(
                    PROTO_VERO_2_CONFIGURATION
                ).build()
            )
            .build()

        val proto = projectConfigSharedPrefsMigration.migrate(protoProjectConfiguration)
        assertThat(proto).isEqualTo(expectedProto)
    }

    private fun concatMapsAsString(vararg maps: Map<String, String>): String {
        val m = mutableMapOf<String, String>()
        maps.forEach {
            it.keys.forEach { key ->
                m[key] = it[key]!!
            }
        }
        return jacksonObjectMapper().writeValueAsString(m)
    }

    companion object {
        private const val PROJECT_ID = "projectId"

        private val JSON_GENERAL_CONFIGURATION =
            jacksonObjectMapper().readValue<Map<String, String>>(
                "{\"EnrolmentPlus\":\"false\",\"LocationRequired\":\"true\",\"Modality\":\"FACE,FINGER\",\"ProjectLanguages\":\"en,fr,pt\",\"ProjectSpecificMode\":\"true\",\"SelectedLanguage\":\"en\"}"
            )

        private val PROTO_GENERAL_CONFIGURATION = ProtoGeneralConfiguration.newBuilder()
            .addAllModalities(
                listOf(
                    ProtoGeneralConfiguration.Modality.FACE,
                    ProtoGeneralConfiguration.Modality.FINGERPRINT
                )
            )
            .addAllLanguageOptions(listOf("en", "fr", "pt"))
            .setDefaultLanguage("en")
            .setCollectLocation(true)
            .setDuplicateBiometricEnrolmentCheck(false)
            .build()

        private val JSON_CONSENT_CONFIGURATION =
            jacksonObjectMapper().readValue<Map<String, String>>(
                "{\"ConsentGeneralOptions\":\"{\\\"consent_enrol_only\\\":false,\\\"consent_enrol\\\":true,\\\"consent_id_verify\\\":true,\\\"consent_share_data_no\\\":false,\\\"consent_share_data_yes\\\":true,\\\"consent_collect_yes\\\":true,\\\"consent_privacy_rights\\\":true,\\\"consent_confirmation\\\":true}\",\"ConsentParentalExists\":\"true\",\"ConsentParentalOptions\":\"{\\\"consent_parent_enrol_only\\\":true,\\\"consent_parent_enrol\\\":false,\\\"consent_parent_id_verify\\\":true,\\\"consent_parent_share_data_no\\\":true,\\\"consent_parent_share_data_yes\\\":false,\\\"consent_parent_collect_yes\\\":false,\\\"consent_parent_privacy_rights\\\":false,\\\"consent_parent_confirmation\\\":false}\",\"ConsentRequired\":\"true\",\"LogoExists\":\"true\",\"OrganizationName\":\"organization name\",\"ProgramName\":\"program name\"}"
            )
        private val PROTO_CONSENT_CONFIGURATION = ProtoConsentConfiguration.newBuilder()
            .setProgramName("program name")
            .setOrganizationName("organization name")
            .setCollectConsent(true)
            .setDisplaySimprintsLogo(true)
            .setAllowParentalConsent(true)
            .setGeneralPrompt(
                ProtoConsentConfiguration.ConsentPromptConfiguration.newBuilder()
                    .setEnrolmentVariant(ProtoConsentConfiguration.ConsentEnrolmentVariant.STANDARD)
                    .setDataSharedWithPartner(true)
                    .setDataUsedForRAndD(true)
                    .setPrivacyRights(true)
                    .setConfirmation(true)
                    .build()
            )
            .setParentalPrompt(
                ProtoConsentConfiguration.ConsentPromptConfiguration.newBuilder()
                    .setEnrolmentVariant(ProtoConsentConfiguration.ConsentEnrolmentVariant.ENROLMENT_ONLY)
                    .setDataSharedWithPartner(false)
                    .setDataUsedForRAndD(false)
                    .setPrivacyRights(false)
                    .setConfirmation(false)
                    .build()
            )
            .build()

        private val JSON_SYNCHRONIZATION_CONFIGURATION =
            jacksonObjectMapper().readValue<Map<String, String>>(
                "{\"CoSync\":\"ONLY_ANALYTICS\",\"DownSyncSetting\":\"ON\",\"MaxNbOfModules\":\"5\",\"ModuleIdOptions\":\"module1|module2\",\"SimprintsSync\":\"ALL\",\"SyncDestination\":\"SIMPRINTS,COMMCARE\",\"SyncGroup\":\"GLOBAL\"}"
            )
        private val PROTO_SYNCHRONIZATION_CONFIGURATION =
            ProtoSynchronizationConfiguration.newBuilder()
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
                                .setKind(ProtoUpSynchronizationConfiguration.UpSynchronizationKind.ONLY_ANALYTICS)
                                .build()
                        )
                        .build()
                )
                .setDown(
                    ProtoDownSynchronizationConfiguration.newBuilder()
                        .setPartitionType(ProtoDownSynchronizationConfiguration.PartitionType.PROJECT)
                        .setMaxNbOfModules(5)
                        .addAllModuleOptions(listOf("module1", "module2"))
                )
                .build()

        private val JSON_IDENTIFICATION_CONFIGURATION =
            jacksonObjectMapper().readValue<Map<String, String>>(
                "{\"MatchGroup\":\"MODULE\",\"NbOfIdsInt\":\"4\"}"
            )
        private val PROTO_IDENTIFICATION_CONFIGURATION =
            ProtoIdentificationConfiguration.newBuilder()
                .setMaxNbOfReturnedCandidates(4)
                .setPoolType(ProtoIdentificationConfiguration.PoolType.MODULE)
                .build()

        private val JSON_FACE_CONFIGURATION = jacksonObjectMapper().readValue<Map<String, String>>(
            "{\"FaceConfidenceThresholds\":\"{\\\"LOW\\\":\\\"1\\\",\\\"MEDIUM\\\":\\\"20\\\",\\\"HIGH\\\":\\\"100\\\"}\",\"FaceNbOfFramesCaptured\":\"2\",\"FaceQualityThreshold\":\"-1\",\"SaveFaceImages\":\"true\"}"
        )
        private val PROTO_FACE_CONFIGURATION = ProtoFaceConfiguration.newBuilder()
            .setNbOfImagesToCapture(2)
            .setQualityThreshold(-1)
            .setImageSavingStrategy(ProtoFaceConfiguration.ImageSavingStrategy.ONLY_GOOD_SCAN)
            .setDecisionPolicy(
                ProtoDecisionPolicy.newBuilder().setLow(1).setMedium(20).setHigh(100).build()
            )
            .build()

        private val JSON_VERO_2_CONFIGURATION =
            jacksonObjectMapper().readValue<Map<String, String>>(
                "{\"CaptureFingerprintStrategy\":\"SECUGEN_ISO_500_DPI\",\"FingerComparisonStrategyForVerification\":\"SAME_FINGER\",\"FingerprintLiveFeedbackOn\":\"true\",\"SaveFingerprintImagesStrategy\":\"WSQ_15_EAGER\",\"Vero2FirmwareVersions\":\"{\\\"E-1\\\":{\\\"cypress\\\":\\\"1.1\\\",\\\"stm\\\":\\\"1.0\\\",\\\"un20\\\":\\\"1.3\\\"}}\"}"
            )
        private val PROTO_VERO_2_CONFIGURATION = ProtoVero2Configuration.newBuilder()
            .setCaptureStrategy(ProtoVero2Configuration.CaptureStrategy.SECUGEN_ISO_500_DPI)
            .setImageSavingStrategy(ProtoVero2Configuration.ImageSavingStrategy.EAGER)
            .setDisplayLiveFeedback(true)
            .putAllFirmwareVersions(
                mapOf(
                    "E-1" to ProtoVero2Configuration.Vero2FirmwareVersions.newBuilder()
                        .setCypress("1.1")
                        .setStm("1.0").setUn20("1.3").build()
                )
            )
            .build()

        private val JSON_FINGERPRINT_CONFIGURATION =
            jacksonObjectMapper().readValue<Map<String, String>>(
                "{\"FingerComparisonStrategyForVerification\":\"SAME_FINGER\",\"FingerImagesExist\":\"true\",\"FingerStatus\":\"{\\\"RIGHT_5TH_FINGER\\\":\\\"false\\\",\\\"RIGHT_4TH_FINGER\\\":\\\"false\\\",\\\"RIGHT_3RD_FINGER\\\":\\\"false\\\",\\\"RIGHT_INDEX_FINGER\\\":\\\"false\\\",\\\"RIGHT_THUMB\\\":\\\"false\\\",\\\"LEFT_THUMB\\\":\\\"true\\\",\\\"LEFT_INDEX_FINGER\\\":\\\"true\\\",\\\"LEFT_3RD_FINGER\\\":\\\"false\\\",\\\"LEFT_4TH_FINGER\\\":\\\"false\\\",\\\"LEFT_5TH_FINGER\\\":\\\"false\\\"}\",\"FingerprintConfidenceThresholds\":\"{\\\"LOW\\\":\\\"10\\\",\\\"MEDIUM\\\":\\\"40\\\",\\\"HIGH\\\":\\\"200\\\"}\",\"FingerprintQualityThreshold\":\"60\",\"FingerprintsToCollect\":\"LEFT_INDEX_FINGER,LEFT_INDEX_FINGER,LEFT_THUMB\",\"ScannerGenerations\":\"VERO_1,VERO_2\"}"
            )
        private val PROTO_FINGERPRINT_CONFIGURATION = ProtoFingerprintConfiguration.newBuilder()
            .addAllFingersToCapture(
                listOf(
                    ProtoFingerprintConfiguration.Finger.LEFT_INDEX_FINGER,
                    ProtoFingerprintConfiguration.Finger.LEFT_INDEX_FINGER,
                    ProtoFingerprintConfiguration.Finger.LEFT_THUMB
                )
            )
            .setQualityThreshold(60)
            .setDecisionPolicy(
                ProtoDecisionPolicy.newBuilder().setLow(10).setMedium(40).setHigh(200).build()
            )
            .addAllAllowedVeroGenerations(
                listOf(
                    ProtoFingerprintConfiguration.VeroGeneration.VERO_1,
                    ProtoFingerprintConfiguration.VeroGeneration.VERO_2
                )
            )
            .setComparisonStrategyForVerification(ProtoFingerprintConfiguration.FingerComparisonStrategy.SAME_FINGER)
            .setDisplayHandIcons(true)
            .build()
    }
}
