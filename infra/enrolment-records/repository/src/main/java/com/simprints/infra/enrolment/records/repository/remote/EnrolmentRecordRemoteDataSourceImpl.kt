package com.simprints.infra.enrolment.records.repository.remote

import com.simprints.core.tools.utils.EncodingUtils
import com.simprints.core.tools.utils.EncodingUtilsImpl
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.enrolment.records.repository.domain.models.Subject
import com.simprints.infra.enrolment.records.repository.remote.models.ApiEnrolmentRecords
import com.simprints.infra.enrolment.records.repository.remote.models.toEnrolmentRecord
import com.simprints.infra.network.SimNetwork
import javax.inject.Inject

internal class EnrolmentRecordRemoteDataSourceImpl(
    private val authStore: AuthStore,
    private val encoder: EncodingUtils = EncodingUtilsImpl,
) : EnrolmentRecordRemoteDataSource {
    @Inject
    constructor(authStore: AuthStore) : this(authStore, EncodingUtilsImpl)

    override suspend fun uploadRecords(subjects: List<Subject>) {
        val projectId = authStore.signedInProjectId

        return getClient().executeCall { apiInterface ->
            apiInterface.uploadRecords(
                projectId,
                ApiEnrolmentRecords(subjects.map { it.toEnrolmentRecord(encoder) }),
            )
        }
    }

    private suspend fun getClient(): SimNetwork.SimApiClient<EnrolmentRecordApiInterface> =
        authStore.buildClient(EnrolmentRecordApiInterface::class)
}
