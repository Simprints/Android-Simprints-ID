package com.simprints.id.enrolmentrecords.remote

import com.simprints.id.enrolmentrecords.remote.models.ApiEnrolmentRecords
import com.simprints.infra.network.SimRemoteInterface
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface EnrolmentRecordApiInterface : SimRemoteInterface {

    @POST("projects/{projectId}/enrolment-records")
    suspend fun uploadRecords(
        @Path("projectId") projectId: String,
        @Body records: ApiEnrolmentRecords,
    )
}
