package com.simprints.infra.images.remote.signedurl.api

import androidx.annotation.Keep
import com.simprints.infra.network.SimRemoteInterface
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Url

@Keep
internal interface SampleUploadApiInterface : SimRemoteInterface {
    @POST("projects/{projectId}/samples")
    suspend fun getSampleUploadUrl(
        @Path("projectId") projectId: String,
        @Body samples: List<ApiSampleUploadUrlRequest>,
    ): List<ApiSampleUploadUrlResponse>

    /**
     * Uploads the provided sample to the provided url.
     *
     * Since sample files are stored in an encrypted files and are accessed via non-resettable
     * input stream, body logging will consume the stream and the actual upload will fail.
     *
     * **Therefore for this upload to work the highest log level is HEADER.**
     */
    @PUT
    suspend fun uploadFile(
        @Url uploadUrl: String,
        @Header("X-Request-ID") requestId: String,
        @Header("Content-MD5") md5: String,
        @Body requestBody: SampleUploadRequestBody,
    ): Response<ResponseBody>
}
