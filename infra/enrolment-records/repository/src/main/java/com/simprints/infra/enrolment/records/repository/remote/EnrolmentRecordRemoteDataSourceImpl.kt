package com.simprints.infra.enrolment.records.repository.remote

import com.simprints.core.tools.utils.EncodingUtils
import com.simprints.core.tools.utils.EncodingUtilsImpl
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.backendapi.BackendApiClient
import com.simprints.infra.enrolment.records.repository.domain.models.EnrolmentRecord
import com.simprints.infra.enrolment.records.repository.remote.models.ApiEnrolmentRecords
import com.simprints.infra.enrolment.records.repository.remote.models.toEnrolmentRecord
import javax.inject.Inject

internal class EnrolmentRecordRemoteDataSourceImpl @Inject constructor(
    private val authStore: AuthStore,
    private val backendApiClient: BackendApiClient,
    private val encoder: EncodingUtils = EncodingUtilsImpl,
) : EnrolmentRecordRemoteDataSource {
    override suspend fun uploadRecords(enrolmentRecords: List<EnrolmentRecord>) {
        val projectId = authStore.signedInProjectId

        return backendApiClient
            .executeCall(EnrolmentRecordApiInterface::class) { apiInterface ->
                apiInterface.uploadRecords(
                    projectId,
                    ApiEnrolmentRecords(enrolmentRecords.map { it.toEnrolmentRecord(encoder) }),
                )
            }
    }
}
