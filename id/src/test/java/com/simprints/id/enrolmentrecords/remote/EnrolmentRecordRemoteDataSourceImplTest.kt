package com.simprints.id.enrolmentrecords.remote

import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.face.FaceSample
import com.simprints.core.domain.fingerprint.FingerprintSample
import com.simprints.core.tools.utils.EncodingUtils
import com.simprints.id.data.db.subject.domain.Subject
import com.simprints.id.enrolmentrecords.remote.models.ApiEnrolmentRecord
import com.simprints.id.enrolmentrecords.remote.models.ApiEnrolmentRecords
import com.simprints.id.enrolmentrecords.remote.models.face.ApiFaceReference
import com.simprints.id.enrolmentrecords.remote.models.face.ApiFaceTemplate
import com.simprints.id.enrolmentrecords.remote.models.face.ApiFaceTemplateFormat
import com.simprints.id.enrolmentrecords.remote.models.fingerprint.ApiFinger
import com.simprints.id.enrolmentrecords.remote.models.fingerprint.ApiFingerprintReference
import com.simprints.id.enrolmentrecords.remote.models.fingerprint.ApiFingerprintTemplate
import com.simprints.id.enrolmentrecords.remote.models.fingerprint.ApiFingerprintTemplateFormat
import com.simprints.infra.login.LoginManager
import com.simprints.infra.network.SimNetwork
import com.simprints.infra.network.exceptions.BackendMaintenanceException
import com.simprints.infra.network.exceptions.SyncCloudIntegrationException
import com.simprints.moduleapi.face.responses.entities.IFaceTemplateFormat
import com.simprints.moduleapi.fingerprint.IFingerIdentifier
import com.simprints.moduleapi.fingerprint.IFingerprintTemplateFormat
import com.simprints.testtools.common.alias.InterfaceInvocation
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class EnrolmentRecordRemoteDataSourceImplTest {

    companion object {
        private const val PROJECT_ID = "projectId"
        private const val SUBJECT_ID = "subjectId"
        private const val MODULE_ID = "moduleId"
        private const val ATTENDANT_ID = "attendantId"
        private val FINGERPRINT_TEMPLATE = byteArrayOf(1, 2, 3)
        private const val BASE64_FINGERPRINT_TEMPLATE = "base64Fingerprint"
        private val FACE_TEMPLATE = byteArrayOf(4, 5, 6)
        private const val BASE64_FACE_TEMPLATE = "base64Face"
    }

    private val remoteInterface = mockk<EnrolmentRecordApiInterface>()
    private val simApiClient = mockk<SimNetwork.SimApiClient<EnrolmentRecordApiInterface>>()
    private val encodingUtils = mockk<EncodingUtils> {
        every { byteArrayToBase64(FINGERPRINT_TEMPLATE) } returns BASE64_FINGERPRINT_TEMPLATE
        every { byteArrayToBase64(FACE_TEMPLATE) } returns BASE64_FACE_TEMPLATE
    }
    private val loginManager = mockk<LoginManager> {
        every { getSignedInProjectIdOrEmpty() } returns PROJECT_ID
        coEvery { buildClient(EnrolmentRecordApiInterface::class) } returns simApiClient
    }
    private val enrolmentRecordRemoteDataSourceImpl =
        EnrolmentRecordRemoteDataSourceImpl(loginManager, encodingUtils)

    @Before
    fun setup() {
        coEvery { simApiClient.executeCall<Unit>(any()) } coAnswers {
            val args = this.args
            @Suppress("UNCHECKED_CAST")
            (args[0] as InterfaceInvocation<EnrolmentRecordApiInterface, Unit>).invoke(
                remoteInterface
            )
        }
    }

    @Test
    fun `Upload successfully the records`() = runTest {
        val subject = Subject(
            subjectId = SUBJECT_ID,
            projectId = PROJECT_ID,
            moduleId = MODULE_ID,
            attendantId = ATTENDANT_ID,
            fingerprintSamples = listOf(
                FingerprintSample(
                    IFingerIdentifier.LEFT_3RD_FINGER,
                    FINGERPRINT_TEMPLATE,
                    50,
                    IFingerprintTemplateFormat.ISO_19794_2
                )
            ),
            faceSamples = listOf(
                FaceSample(
                    FACE_TEMPLATE,
                    IFaceTemplateFormat.RANK_ONE_1_23
                )
            )
        )
        val expectedRecord = ApiEnrolmentRecord(
            subjectId = SUBJECT_ID,
            moduleId = MODULE_ID,
            attendantId = ATTENDANT_ID,
            biometricReferences = listOf(
                ApiFingerprintReference(
                    id = "5289df73-7df5-3326-bcdd-22597afb1fac",
                    templates = listOf(
                        ApiFingerprintTemplate(
                            quality = 50,
                            template = BASE64_FINGERPRINT_TEMPLATE,
                            finger = ApiFinger.LEFT_3RD_FINGER,
                        )
                    ),
                    format = ApiFingerprintTemplateFormat.ISO_19794_2
                ),
                ApiFaceReference(
                    id = "b4a3ba90-6413-32b4-a4ea-a841a5a400ec",
                    templates = listOf(
                        ApiFaceTemplate(
                            template = BASE64_FACE_TEMPLATE,
                        )
                    ),
                    format = ApiFaceTemplateFormat.RANK_ONE_1_23,
                )
            )
        )
        enrolmentRecordRemoteDataSourceImpl.uploadRecords(listOf(subject))

        coVerify(exactly = 1) {
            remoteInterface.uploadRecords(
                PROJECT_ID,
                ApiEnrolmentRecords(listOf(expectedRecord))
            )
        }
    }

    @Test
    fun `Fail to upload enrolment records if backend maintenance exception`() = runTest {
        val exception = BackendMaintenanceException(estimatedOutage = 100)
        coEvery { remoteInterface.uploadRecords(PROJECT_ID, any()) } throws exception

        val receivedException = assertThrows<BackendMaintenanceException> {
            enrolmentRecordRemoteDataSourceImpl.uploadRecords(listOf())
        }
        assertThat(receivedException).isEqualTo(exception)
    }

    @Test
    fun `Fail to upload enrolment records if sync cloud integration exception`() = runTest {
        val exception = SyncCloudIntegrationException(cause = Exception())
        coEvery { remoteInterface.uploadRecords(PROJECT_ID, any()) } throws exception

        val receivedException = assertThrows<SyncCloudIntegrationException> {
            enrolmentRecordRemoteDataSourceImpl.uploadRecords(listOf())
        }
        assertThat(receivedException).isEqualTo(exception)
    }


}
