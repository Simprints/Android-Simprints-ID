package com.simprints.infra.sync.devicestate.remote

import com.simprints.infra.network.SimRemoteInterface
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

internal interface DeviceStatusRemoteInterface : SimRemoteInterface {
    @POST("projects/{projectId}/devices/{deviceId}/status")
    suspend fun postDeviceStatus(
        @Path("projectId") projectId: String,
        @Path("deviceId") deviceId: String,
        @Body deviceStatus: ApiDeviceDataState,
    ): Response<ResponseBody>
}
