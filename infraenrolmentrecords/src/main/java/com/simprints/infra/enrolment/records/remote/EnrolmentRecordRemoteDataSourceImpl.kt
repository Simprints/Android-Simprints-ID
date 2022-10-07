package com.simprints.infra.enrolment.records.remote

import com.simprints.core.tools.utils.EncodingUtils
import com.simprints.core.tools.utils.EncodingUtilsImpl
import com.simprints.infra.enrolment.records.domain.models.Subject
import com.simprints.infra.enrolment.records.remote.models.ApiEnrolmentRecords
import com.simprints.infra.enrolment.records.remote.models.toEnrolmentRecord
import com.simprints.infra.login.LoginManager
import com.simprints.infra.network.SimNetwork
import javax.inject.Inject

internal class EnrolmentRecordRemoteDataSourceImpl(
    private val loginManager: LoginManager,
    private val encoder: EncodingUtils = EncodingUtilsImpl,
) :
    EnrolmentRecordRemoteDataSource {

    @Inject
    constructor(loginManager: LoginManager) : this(loginManager, EncodingUtilsImpl)

    override suspend fun uploadRecords(subjects: List<Subject>) {
        val projectId = loginManager.getSignedInProjectIdOrEmpty()

        return getClient().executeCall { apiInterface ->
            apiInterface.uploadRecords(
                projectId,
                ApiEnrolmentRecords(subjects.map { it.toEnrolmentRecord(encoder) })
            )
        }
    }

    private suspend fun getClient(): SimNetwork.SimApiClient<EnrolmentRecordApiInterface> {
        return loginManager.buildClient(EnrolmentRecordApiInterface::class)
    }
}
