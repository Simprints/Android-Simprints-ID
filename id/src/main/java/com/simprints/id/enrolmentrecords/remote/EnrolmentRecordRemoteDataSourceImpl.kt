package com.simprints.id.enrolmentrecords.remote

import com.simprints.core.tools.utils.EncodingUtils
import com.simprints.core.tools.utils.EncodingUtilsImpl
import com.simprints.id.data.db.subject.domain.Subject
import com.simprints.id.enrolmentrecords.remote.models.ApiEnrolmentRecords
import com.simprints.id.enrolmentrecords.remote.models.toEnrolmentRecord
import com.simprints.infra.login.LoginManager
import com.simprints.infra.network.SimNetwork

class EnrolmentRecordRemoteDataSourceImpl(
    private val loginManager: LoginManager,
    private val encoder: EncodingUtils = EncodingUtilsImpl,
) :
    EnrolmentRecordRemoteDataSource {

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
