package com.simprints.infra.enrolment.records.repository.remote

import com.simprints.infra.enrolment.records.repository.remote.models.ApiEnrolmentRecords
import com.simprints.infra.network.SimRemoteInterface
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

internal interface EnrolmentRecordApiInterface : SimRemoteInterface {
    @Headers("Content-Encoding: gzip")
    @POST("projects/{projectId}/enrolment-records")
    suspend fun uploadRecords(
        @Path("projectId") projectId: String,
        @Body records: ApiEnrolmentRecords,
    )
}
