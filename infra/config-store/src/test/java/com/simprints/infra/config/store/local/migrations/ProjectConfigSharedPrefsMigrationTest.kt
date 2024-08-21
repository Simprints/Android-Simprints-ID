package com.simprints.infra.config.store.local.migrations

import android.content.Context
import android.content.SharedPreferences
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.json.JsonHelper
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.local.migrations.ProjectConfigSharedPrefsMigration.Companion.ALL_KEYS
import com.simprints.infra.config.store.local.migrations.ProjectConfigSharedPrefsMigration.Companion.PROJECT_SETTINGS_JSON_STRING_KEY
import com.simprints.infra.config.store.local.migrations.models.OldProjectConfig
import com.simprints.infra.config.store.local.models.ProtoAllowedAgeRange
import com.simprints.infra.config.store.local.models.ProtoConsentConfiguration
import com.simprints.infra.config.store.local.models.ProtoDecisionPolicy
import com.simprints.infra.config.store.local.models.ProtoDownSynchronizationConfiguration
import com.simprints.infra.config.store.local.models.ProtoFaceConfiguration
import com.simprints.infra.config.store.local.models.ProtoFinger
import com.simprints.infra.config.store.local.models.ProtoFingerprintConfiguration
import com.simprints.infra.config.store.local.models.ProtoGeneralConfiguration
import com.simprints.infra.config.store.local.models.ProtoIdentificationConfiguration
import com.simprints.infra.config.store.local.models.ProtoProjectConfiguration
import com.simprints.infra.config.store.local.models.ProtoSynchronizationConfiguration
import com.simprints.infra.config.store.local.models.ProtoUpSyncBatchSizes
import com.simprints.infra.config.store.local.models.ProtoUpSynchronizationConfiguration
import com.simprints.infra.config.store.local.models.ProtoVero1Configuration
import com.simprints.infra.config.store.local.models.ProtoVero2Configuration
import com.simprints.infra.config.store.testtools.protoProjectConfiguration
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProjectConfigSharedPrefsMigrationTest {

    private val ctx = mockk<Context>()
    private val editor = mockk<SharedPreferences.Editor>(relaxed = true)
    private val preferences = mockk<SharedPreferences>(relaxed = true) {
        every { edit() } returns editor
    }
    private val authStore = mockk<AuthStore>()
    private lateinit var projectConfigSharedPrefsMigration: ProjectConfigSharedPrefsMigration

    @Before
    fun setup() {
        every { ctx.getSharedPreferences(any(), any()) } returns preferences
        projectConfigSharedPrefsMigration = ProjectConfigSharedPrefsMigration(ctx, authStore)
    }

    @Test
    fun `shouldMigrate should return true only if the current data is empty and the shared pref is not empty`() =
        runTest {
            every { preferences.getString(PROJECT_SETTINGS_JSON_STRING_KEY, "") } returns "{}"

            val shouldMigrate =
                projectConfigSharedPrefsMigration.shouldMigrate(ProtoProjectConfiguration.getDefaultInstance())
            assertThat(shouldMigrate).isTrue()
        }

    @Test
    fun `shouldMigrate should return false if the current data is not empty`() =
        runTest {
            every { preferences.getString(PROJECT_SETTINGS_JSON_STRING_KEY, "") } returns "{}"

            val shouldMigrate =
                projectConfigSharedPrefsMigration.shouldMigrate(protoProjectConfiguration)
            assertThat(shouldMigrate).isFalse()
        }

    @Test
    fun `shouldMigrate should return false if the current data is empty`() =
        runTest {
            every { preferences.getString(PROJECT_SETTINGS_JSON_STRING_KEY, "") } returns ""

            val shouldMigrate = projectConfigSharedPrefsMigration.shouldMigrate(
                ProtoProjectConfiguration.getDefaultInstance()
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
        every { authStore.signedInProjectId } returns PROJECT_ID

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
        every { authStore.signedInProjectId } returns PROJECT_ID

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
    fun `migrate should work when face is present without some fields`() = runTest {
        val json = concatMapsAsString(
            JSON_GENERAL_CONFIGURATION,
            JSON_CONSENT_CONFIGURATION,
            JSON_IDENTIFICATION_CONFIGURATION,
            JSON_SYNCHRONIZATION_CONFIGURATION,
            JSON_FACE_CONFIGURATION_WITHOUT_FIELDS
        )
        every { preferences.getString(any(), any()) } returns json
        every { authStore.signedInProjectId } returns PROJECT_ID

        val expectedProto = ProtoProjectConfiguration.newBuilder()
            .setProjectId(PROJECT_ID)
            .setConsent(PROTO_CONSENT_CONFIGURATION)
            .setGeneral(PROTO_GENERAL_CONFIGURATION)
            .setIdentification(PROTO_IDENTIFICATION_CONFIGURATION)
            .setSynchronization(PROTO_SYNCHRONIZATION_CONFIGURATION)
            .setFace(PROTO_FACE_DEFAULT_CONFIGURATION)
            .build()

        val proto = projectConfigSharedPrefsMigration.migrate(protoProjectConfiguration)
        assertThat(proto).isEqualTo(expectedProto)
    }

    @Test
    fun `migrate should work when fingerprint is present without some fields`() = runTest {
        val json = concatMapsAsString(
            JSON_GENERAL_CONFIGURATION,
            JSON_CONSENT_CONFIGURATION,
            JSON_IDENTIFICATION_CONFIGURATION,
            JSON_SYNCHRONIZATION_CONFIGURATION,
            JSON_FINGERPRINT_CONFIGURATION_WITHOUT_FIELDS
        )
        every { preferences.getString(any(), any()) } returns json
        every { authStore.signedInProjectId } returns PROJECT_ID

        val expectedProto = ProtoProjectConfiguration.newBuilder()
            .setProjectId(PROJECT_ID)
            .setConsent(PROTO_CONSENT_CONFIGURATION)
            .setGeneral(PROTO_GENERAL_CONFIGURATION)
            .setIdentification(PROTO_IDENTIFICATION_CONFIGURATION)
            .setSynchronization(PROTO_SYNCHRONIZATION_CONFIGURATION)
            .setFingerprint(PROTO_FINGERPRINT_DEFAULT_CONFIGURATION)
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
        every { authStore.signedInProjectId } returns PROJECT_ID

        val expectedProto = ProtoProjectConfiguration.newBuilder()
            .setProjectId(PROJECT_ID)
            .setConsent(PROTO_CONSENT_CONFIGURATION)
            .setGeneral(PROTO_GENERAL_CONFIGURATION)
            .setIdentification(PROTO_IDENTIFICATION_CONFIGURATION)
            .setSynchronization(PROTO_SYNCHRONIZATION_CONFIGURATION)
            .setFingerprint(
                PROTO_FINGERPRINT_CONFIGURATION.toBuilder().setSecugenSimMatcher(
                    PROTO_FINGERPRINT_CONFIGURATION.secugenSimMatcher.toBuilder().clearVero2()
                        .build()
                )
            )
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
        every { authStore.signedInProjectId } returns PROJECT_ID

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
        every { authStore.signedInProjectId } returns PROJECT_ID

        val expectedProto = ProtoProjectConfiguration.newBuilder()
            .setProjectId(PROJECT_ID)
            .setConsent(PROTO_CONSENT_CONFIGURATION)
            .setGeneral(PROTO_GENERAL_CONFIGURATION)
            .setIdentification(PROTO_IDENTIFICATION_CONFIGURATION)
            .setSynchronization(PROTO_SYNCHRONIZATION_CONFIGURATION)
            .setFace(PROTO_FACE_CONFIGURATION)
            .setFingerprint(PROTO_FINGERPRINT_CONFIGURATION)
            .build()

        val proto = projectConfigSharedPrefsMigration.migrate(protoProjectConfiguration)
        assertThat(proto).isEqualTo(expectedProto)
    }

    @Test
    fun `migrate should work when SyncDestination is null`() = runTest {
        val json = concatMapsAsString(
            JSON_GENERAL_CONFIGURATION,
            JSON_CONSENT_CONFIGURATION,
            JSON_IDENTIFICATION_CONFIGURATION,
            JSON_SYNCHRONIZATION_CONFIGURATION_NULL_VALUES,
            JSON_FACE_CONFIGURATION,
            JSON_FINGERPRINT_CONFIGURATION,
            JSON_VERO_2_CONFIGURATION
        )
        every { preferences.getString(any(), any()) } returns json
        every { authStore.signedInProjectId } returns PROJECT_ID

        val expectedProto = ProtoProjectConfiguration.newBuilder()
            .setProjectId(PROJECT_ID)
            .setConsent(PROTO_CONSENT_CONFIGURATION)
            .setGeneral(PROTO_GENERAL_CONFIGURATION)
            .setIdentification(PROTO_IDENTIFICATION_CONFIGURATION)
            .setSynchronization(PROTO_SYNCHRONIZATION_CONFIGURATION_NULL_VALUES)
            .setFace(PROTO_FACE_CONFIGURATION)
            .setFingerprint(PROTO_FINGERPRINT_CONFIGURATION)
            .build()

        val proto = projectConfigSharedPrefsMigration.migrate(protoProjectConfiguration)
        assertThat(proto).isEqualTo(expectedProto)
    }

    @Test
    fun `migrate should work when empty SyncDestination`() = runTest {
        val json = concatMapsAsString(
            JSON_GENERAL_CONFIGURATION,
            JSON_CONSENT_CONFIGURATION,
            JSON_IDENTIFICATION_CONFIGURATION,
            JSON_SYNCHRONIZATION_CONFIGURATION_EMPTY_SYNC_DESTINATION,
            JSON_FACE_CONFIGURATION,
            JSON_FINGERPRINT_CONFIGURATION,
            JSON_VERO_2_CONFIGURATION
        )
        every { preferences.getString(any(), any()) } returns json
        every { authStore.signedInProjectId } returns PROJECT_ID

        val expectedProto = ProtoProjectConfiguration.newBuilder()
            .setProjectId(PROJECT_ID)
            .setConsent(PROTO_CONSENT_CONFIGURATION)
            .setGeneral(PROTO_GENERAL_CONFIGURATION)
            .setIdentification(PROTO_IDENTIFICATION_CONFIGURATION)
            .setSynchronization(PROTO_SYNCHRONIZATION_CONFIGURATION_EMPTY_VALUES)
            .setFace(PROTO_FACE_CONFIGURATION)
            .setFingerprint(PROTO_FINGERPRINT_CONFIGURATION)
            .build()

        val proto = projectConfigSharedPrefsMigration.migrate(protoProjectConfiguration)
        assertThat(proto).isEqualTo(expectedProto)
    }

    @Test
    fun `migrate should work when non empty SyncDestination`() = runTest {
        val json = concatMapsAsString(
            JSON_GENERAL_CONFIGURATION,
            JSON_CONSENT_CONFIGURATION,
            JSON_IDENTIFICATION_CONFIGURATION,
            JSON_SYNCHRONIZATION_CONFIGURATION_NON_EMPTY_SYNC_DESTINATION,
            JSON_FACE_CONFIGURATION,
            JSON_FINGERPRINT_CONFIGURATION,
            JSON_VERO_2_CONFIGURATION
        )
        every { preferences.getString(any(), any()) } returns json
        every { authStore.signedInProjectId } returns PROJECT_ID

        val expectedProto = ProtoProjectConfiguration.newBuilder()
            .setProjectId(PROJECT_ID)
            .setConsent(PROTO_CONSENT_CONFIGURATION)
            .setGeneral(PROTO_GENERAL_CONFIGURATION)
            .setIdentification(PROTO_IDENTIFICATION_CONFIGURATION)
            .setSynchronization(PROTO_SYNCHRONIZATION_CONFIGURATION_NON_NULL_VALUES)
            .setFace(PROTO_FACE_CONFIGURATION)
            .setFingerprint(PROTO_FINGERPRINT_CONFIGURATION)
            .build()

        val proto = projectConfigSharedPrefsMigration.migrate(protoProjectConfiguration)
        assertThat(proto).isEqualTo(expectedProto)
    }

    @Test
    fun `migrate should work when there is unexpected fields`() = runTest {
        val json = concatMapsAsString(
            JSON_GENERAL_CONFIGURATION,
            JSON_CONSENT_CONFIGURATION,
            JSON_IDENTIFICATION_CONFIGURATION,
            JSON_SYNCHRONIZATION_CONFIGURATION,
            JSON_FACE_CONFIGURATION_WITH_UNEXPECTED_FIELD,
            JSON_FINGERPRINT_CONFIGURATION,
            JSON_VERO_2_CONFIGURATION
        )
        every { preferences.getString(any(), any()) } returns json
        every { authStore.signedInProjectId } returns PROJECT_ID

        val expectedProto = ProtoProjectConfiguration.newBuilder()
            .setProjectId(PROJECT_ID)
            .setConsent(PROTO_CONSENT_CONFIGURATION)
            .setGeneral(PROTO_GENERAL_CONFIGURATION)
            .setIdentification(PROTO_IDENTIFICATION_CONFIGURATION)
            .setSynchronization(PROTO_SYNCHRONIZATION_CONFIGURATION)
            .setFace(PROTO_FACE_CONFIGURATION)
            .setFingerprint(PROTO_FINGERPRINT_CONFIGURATION)
            .build()

        val proto = projectConfigSharedPrefsMigration.migrate(protoProjectConfiguration)
        assertThat(proto).isEqualTo(expectedProto)
    }

    @Test
    fun `migrate should return the default project configuration if the existing config is invalid`() =
        runTest {
            every { preferences.getString(any(), any()) } returns "{invalidJson}"
            every { authStore.signedInProjectId } returns PROJECT_ID

            val proto = projectConfigSharedPrefsMigration.migrate(protoProjectConfiguration)
            assertThat(proto).isEqualTo(ProtoProjectConfiguration.getDefaultInstance())
        }

    @Test
    fun `migrate should throw an exception if there is an issue that is not related to jackson`() =
        runTest {
            val json = concatMapsAsString(
                JSON_GENERAL_CONFIGURATION,
                JSON_CONSENT_CONFIGURATION,
                JSON_IDENTIFICATION_CONFIGURATION,
                JSON_SYNCHRONIZATION_CONFIGURATION,
                JSON_FINGERPRINT_CONFIGURATION
            )
            every { preferences.getString(any(), any()) } returns json
            every { authStore.signedInProjectId } returns PROJECT_ID

            // Force an exception
            val exception = Exception("")
            mockkObject(JsonHelper)
            every { JsonHelper.fromJson<OldProjectConfig>(any()) } throws exception
            val receivedException = assertThrows<Exception> {
                projectConfigSharedPrefsMigration.migrate(protoProjectConfiguration)
            }
            assertThat(receivedException).isEqualTo(exception)
            unmockkAll()
        }

    @Test
    fun `cleanUp should do remove all the keys`() = runTest {
        every { editor.remove(any()) } returns editor

        projectConfigSharedPrefsMigration.cleanUp()

        ALL_KEYS.forEach {
            verify(exactly = 1) { editor.remove(it) }
        }
        verify(exactly = 1) { editor.remove(PROJECT_SETTINGS_JSON_STRING_KEY) }
        verify(exactly = 1) { editor.apply() }
    }

    private fun concatMapsAsString(vararg maps: Map<String, String>): String {
        val m = mutableMapOf<String, String>()
        maps.forEach {
            it.keys.forEach { key ->
                m[key] = it[key]!!
            }
        }

        return JsonHelper.toJson(m)
    }

    companion object {
        private const val PROJECT_ID = "projectId"

        private val JSON_GENERAL_CONFIGURATION =
            JsonHelper.fromJson<Map<String, String>>(
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
            JsonHelper.fromJson<Map<String, String>>(
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

        private val JSON_SYNCHRONIZATION_CONFIGURATION_EMPTY_SYNC_DESTINATION =
            JsonHelper.fromJson<Map<String, String>>(
                "{\"DownSyncSetting\":\"ON\",\"MaxNbOfModules\":\"5\",\"ModuleIdOptions\":\"module1|module2\",\"SyncDestination\":\"\",\"SyncGroup\":\"GLOBAL\"}"
            )
        private val JSON_SYNCHRONIZATION_CONFIGURATION_NON_EMPTY_SYNC_DESTINATION =
            JsonHelper.fromJson<Map<String, String>>(
                "{\"DownSyncSetting\":\"ON\",\"MaxNbOfModules\":\"5\",\"ModuleIdOptions\":\"module1|module2\",\"SimprintsSync\":\"ALL\",\"SyncDestination\":\"SIMPRINTS,COMMCARE\",\"SyncGroup\":\"GLOBAL\"}"
            )
        private val JSON_SYNCHRONIZATION_CONFIGURATION =
            JsonHelper.fromJson<Map<String, String>>(
                "{\"CoSync\":\"ONLY_ANALYTICS\",\"DownSyncSetting\":\"ON\",\"MaxNbOfModules\":\"5\",\"ModuleIdOptions\":\"module1|module2\",\"SimprintsSync\":\"ALL\",\"SyncDestination\":\"SIMPRINTS,COMMCARE\",\"SyncGroup\":\"GLOBAL\"}"
            )
        private val JSON_SYNCHRONIZATION_CONFIGURATION_NULL_VALUES =
            JsonHelper.fromJson<Map<String, String>>(
                "{\"DownSyncSetting\":\"ON\",\"MaxNbOfModules\":\"5\",\"ModuleIdOptions\":\"module1|module2\",\"SyncGroup\":\"GLOBAL\"}"
            )
        private val PROTO_SYNCHRONIZATION_CONFIGURATION =
            ProtoSynchronizationConfiguration.newBuilder()
                .setFrequency(ProtoSynchronizationConfiguration.Frequency.PERIODICALLY)
                .setUp(
                    ProtoUpSynchronizationConfiguration.newBuilder()
                        .setSimprints(
                            ProtoUpSynchronizationConfiguration.SimprintsUpSynchronizationConfiguration.newBuilder()
                                .setKind(ProtoUpSynchronizationConfiguration.UpSynchronizationKind.ALL)
                                .setBatchSizes(ProtoUpSyncBatchSizes.newBuilder().setSessions(1).setUpSyncs(1).setDownSyncs(1).build())
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
                        .setMaxAge("PT24H")
                        .build()
                )
                .build()

        private val PROTO_SYNCHRONIZATION_CONFIGURATION_NON_NULL_VALUES =
            ProtoSynchronizationConfiguration.newBuilder()
                .setFrequency(ProtoSynchronizationConfiguration.Frequency.PERIODICALLY)
                .setUp(
                    ProtoUpSynchronizationConfiguration.newBuilder()
                        .setSimprints(
                            ProtoUpSynchronizationConfiguration.SimprintsUpSynchronizationConfiguration.newBuilder()
                                .setKind(ProtoUpSynchronizationConfiguration.UpSynchronizationKind.ALL)
                                .setBatchSizes(ProtoUpSyncBatchSizes.newBuilder().setSessions(1).setUpSyncs(1).setDownSyncs(1).build())
                                .build()
                        )
                        .setCoSync(
                            ProtoUpSynchronizationConfiguration.CoSyncUpSynchronizationConfiguration.newBuilder()
                                .setKind(ProtoUpSynchronizationConfiguration.UpSynchronizationKind.ALL)
                                .build()
                        )
                        .build()
                )
                .setDown(
                    ProtoDownSynchronizationConfiguration.newBuilder()
                        .setPartitionType(ProtoDownSynchronizationConfiguration.PartitionType.PROJECT)
                        .setMaxNbOfModules(5)
                        .addAllModuleOptions(listOf("module1", "module2"))
                        .setMaxAge("PT24H")
                        .build()
                )
                .build()

        private val PROTO_SYNCHRONIZATION_CONFIGURATION_EMPTY_VALUES =
            ProtoSynchronizationConfiguration.newBuilder()
                .setFrequency(ProtoSynchronizationConfiguration.Frequency.PERIODICALLY)
                .setUp(
                    ProtoUpSynchronizationConfiguration.newBuilder()
                        .setSimprints(
                            ProtoUpSynchronizationConfiguration.SimprintsUpSynchronizationConfiguration.newBuilder()
                                .setKind(ProtoUpSynchronizationConfiguration.UpSynchronizationKind.NONE)
                                .setBatchSizes(ProtoUpSyncBatchSizes.newBuilder().setSessions(1).setUpSyncs(1).setDownSyncs(1).build())
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
                        .setMaxNbOfModules(5)
                        .addAllModuleOptions(listOf("module1", "module2"))
                        .setMaxAge("PT24H")
                        .build()
                )
                .build()


        private val PROTO_SYNCHRONIZATION_CONFIGURATION_NULL_VALUES =
            ProtoSynchronizationConfiguration.newBuilder()
                .setFrequency(ProtoSynchronizationConfiguration.Frequency.PERIODICALLY)
                .setUp(
                    ProtoUpSynchronizationConfiguration.newBuilder()
                        .setSimprints(
                            ProtoUpSynchronizationConfiguration.SimprintsUpSynchronizationConfiguration.newBuilder()
                                .setKind(ProtoUpSynchronizationConfiguration.UpSynchronizationKind.NONE)
                                .setBatchSizes(ProtoUpSyncBatchSizes.newBuilder().setSessions(1).setUpSyncs(1).setDownSyncs(1).build())
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
                        .setMaxNbOfModules(5)
                        .addAllModuleOptions(listOf("module1", "module2"))
                        .setMaxAge("PT24H")
                        .build()
                )
                .build()

        private val JSON_IDENTIFICATION_CONFIGURATION =
            JsonHelper.fromJson<Map<String, String>>(
                "{\"MatchGroup\":\"MODULE\",\"NbOfIdsInt\":\"4\"}"
            )
        private val PROTO_IDENTIFICATION_CONFIGURATION =
            ProtoIdentificationConfiguration.newBuilder()
                .setMaxNbOfReturnedCandidates(4)
                .setPoolType(ProtoIdentificationConfiguration.PoolType.MODULE)
                .build()

        private val JSON_FACE_CONFIGURATION = JsonHelper.fromJson<Map<String, String>>(
            "{\"FaceConfidenceThresholds\":\"{\\\"LOW\\\":\\\"1\\\",\\\"MEDIUM\\\":\\\"20\\\",\\\"HIGH\\\":\\\"100\\\"}\",\"FaceNbOfFramesCaptured\":\"2\",\"FaceQualityThreshold\":\"-1\",\"SaveFaceImages\":\"true\"}"
        )
        private val JSON_FACE_CONFIGURATION_WITHOUT_FIELDS =
            JsonHelper.fromJson<Map<String, String>>(
                "{\"FaceQualityThreshold\":\"-1\"}"
            )
        private val JSON_FACE_CONFIGURATION_WITH_UNEXPECTED_FIELD =
            JsonHelper.fromJson<Map<String, String>>(
                "{\"FaceMatchThreshold\":30, \"FaceConfidenceThresholds\":\"{\\\"LOW\\\":\\\"1\\\",\\\"MEDIUM\\\":\\\"20\\\",\\\"HIGH\\\":\\\"100\\\"}\",\"FaceNbOfFramesCaptured\":\"2\",\"FaceQualityThreshold\":\"-1\",\"SaveFaceImages\":\"true\"}"
            )
        private val PROTO_FACE_CONFIGURATION = ProtoFaceConfiguration.newBuilder()
            .addAllowedSdks(ProtoFaceConfiguration.ProtoBioSdk.RANK_ONE)
            .setRankOne(
                ProtoFaceConfiguration.ProtoFaceSdkConfiguration.newBuilder()
                    .setNbOfImagesToCapture(2)
                    .setQualityThreshold(-1)
                    .setImageSavingStrategy(ProtoFaceConfiguration.ImageSavingStrategy.ONLY_USED_IN_REFERENCE)
                    .setDecisionPolicy(
                        ProtoDecisionPolicy.newBuilder().setLow(1).setMedium(20).setHigh(100).build()
                    )
                    .setAllowedAgeRange(ProtoAllowedAgeRange.newBuilder().build())
                    .setVersion("1.23")
                    .build()
            )
            .build()

        private val PROTO_FACE_DEFAULT_CONFIGURATION = ProtoFaceConfiguration.newBuilder()
            .addAllowedSdks(ProtoFaceConfiguration.ProtoBioSdk.RANK_ONE)
            .setRankOne(
                ProtoFaceConfiguration.ProtoFaceSdkConfiguration.newBuilder()
                    .setNbOfImagesToCapture(2)
                    .setQualityThreshold(-1)
                    .setImageSavingStrategy(ProtoFaceConfiguration.ImageSavingStrategy.NEVER)
                    .setDecisionPolicy(
                        ProtoDecisionPolicy.newBuilder().setLow(0).setMedium(0).setHigh(0).build()
                    )
                    .setAllowedAgeRange(ProtoAllowedAgeRange.newBuilder().setStartInclusive(0).build())
                    .setVersion("1.23")
                    .build()
            )
            .build()

        private val JSON_VERO_2_CONFIGURATION =
            JsonHelper.fromJson<Map<String, String>>(
                "{\"CaptureFingerprintStrategy\":\"SECUGEN_ISO_500_DPI\",\"FingerComparisonStrategyForVerification\":\"SAME_FINGER\",\"FingerprintLiveFeedbackOn\":\"true\",\"SaveFingerprintImagesStrategy\":\"WSQ_15_EAGER\",\"Vero2FirmwareVersions\":\"{\\\"E-1\\\":{\\\"cypress\\\":\\\"1.1\\\",\\\"stm\\\":\\\"1.0\\\",\\\"un20\\\":\\\"1.3\\\"}}\"}"
            )
        private val PROTO_VERO_2_CONFIGURATION = ProtoVero2Configuration.newBuilder()
            .setQualityThreshold(60)
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
            JsonHelper.fromJson<Map<String, String>>(
                "{\"FingerComparisonStrategyForVerification\":\"SAME_FINGER\",\"FingerImagesExist\":\"true\",\"FingerStatus\":\"{\\\"RIGHT_5TH_FINGER\\\":\\\"false\\\",\\\"RIGHT_4TH_FINGER\\\":\\\"false\\\",\\\"RIGHT_3RD_FINGER\\\":\\\"false\\\",\\\"RIGHT_INDEX_FINGER\\\":\\\"false\\\",\\\"RIGHT_THUMB\\\":\\\"false\\\",\\\"LEFT_THUMB\\\":\\\"true\\\",\\\"LEFT_INDEX_FINGER\\\":\\\"true\\\",\\\"LEFT_3RD_FINGER\\\":\\\"false\\\",\\\"LEFT_4TH_FINGER\\\":\\\"false\\\",\\\"LEFT_5TH_FINGER\\\":\\\"false\\\"}\",\"FingerprintConfidenceThresholds\":\"{\\\"LOW\\\":\\\"10\\\",\\\"MEDIUM\\\":\\\"40\\\",\\\"HIGH\\\":\\\"200\\\"}\",\"FingerprintQualityThreshold\":\"60\",\"FingerprintsToCollect\":\"LEFT_INDEX_FINGER,LEFT_INDEX_FINGER,LEFT_THUMB\",\"ScannerGenerations\":\"VERO_1,VERO_2\"}"
            )
        private val JSON_FINGERPRINT_CONFIGURATION_WITHOUT_FIELDS =
            JsonHelper.fromJson<Map<String, String>>(
                "{\"FingerprintQualityThreshold\":\"60\"}"
            )
        private val PROTO_FINGERPRINT_CONFIGURATION = ProtoFingerprintConfiguration.newBuilder()
            .addAllAllowedScanners(
                listOf(
                    ProtoFingerprintConfiguration.VeroGeneration.VERO_1,
                    ProtoFingerprintConfiguration.VeroGeneration.VERO_2
                )
            )
            .addAllowedSdks(ProtoFingerprintConfiguration.ProtoBioSdk.SECUGEN_SIM_MATCHER)
            .setDisplayHandIcons(true)
            .setSecugenSimMatcher(
                ProtoFingerprintConfiguration.ProtoFingerprintSdkConfiguration.newBuilder()
                    .addAllFingersToCapture(
                        listOf(
                            ProtoFinger.LEFT_INDEX_FINGER,
                            ProtoFinger.LEFT_INDEX_FINGER,
                            ProtoFinger.LEFT_THUMB
                        )
                    )
                    .setDecisionPolicy(
                        ProtoDecisionPolicy.newBuilder().setLow(10).setMedium(40).setHigh(200)
                            .build()
                    )
                    .setComparisonStrategyForVerification(ProtoFingerprintConfiguration.FingerComparisonStrategy.SAME_FINGER)
                    .setVero1(ProtoVero1Configuration.newBuilder().setQualityThreshold(60).build())
                    .setVero2(PROTO_VERO_2_CONFIGURATION)
                    .setAllowedAgeRange(ProtoAllowedAgeRange.newBuilder().build())
                    .build()
            ).build()

        private val PROTO_FINGERPRINT_DEFAULT_CONFIGURATION =
            ProtoFingerprintConfiguration.newBuilder()
                .addAllAllowedScanners(listOf(ProtoFingerprintConfiguration.VeroGeneration.VERO_1))
                .setDisplayHandIcons(false)
                .addAllowedSdks(ProtoFingerprintConfiguration.ProtoBioSdk.SECUGEN_SIM_MATCHER)
                .setSecugenSimMatcher(
                    ProtoFingerprintConfiguration.ProtoFingerprintSdkConfiguration.newBuilder()
                        .addAllFingersToCapture(
                            listOf(
                                ProtoFinger.LEFT_THUMB,
                                ProtoFinger.LEFT_INDEX_FINGER
                            )
                        )
                        .setDecisionPolicy(
                            ProtoDecisionPolicy.newBuilder().setLow(0).setMedium(0).setHigh(700)
                                .build()
                        )
                        .setComparisonStrategyForVerification(ProtoFingerprintConfiguration.FingerComparisonStrategy.SAME_FINGER)
                        .setVero1(
                            ProtoVero1Configuration.newBuilder().setQualityThreshold(60).build()
                        )
                        .setAllowedAgeRange(ProtoAllowedAgeRange.newBuilder().build())
                        .build()
                ).build()
    }
}
