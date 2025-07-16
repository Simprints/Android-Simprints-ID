package com.simprints.infra.network.httpclient

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.simprints.infra.network.BuildConfig
import com.simprints.logging.persistent.LogEntryType
import com.simprints.logging.persistent.PersistentLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import okio.Buffer
import okio.BufferedSink
import okio.GzipSink
import okio.buffer
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class BuildOkHttpClientUseCase @Inject constructor(
    @ApplicationContext private val ctx: Context,
    private val networkCache: Cache,
    private val persistentLogger: PersistentLogger,
) {
    companion object {
        const val DEVICE_ID_HEADER = "X-Device-ID"
        const val AUTHORIZATION_HEADER = "Authorization"
        const val USER_AGENT_HEADER = "User-Agent"
        const val READ_TIMEOUT = 60L
        const val WRITE_TIMEOUT = 60L

        /**
         * This header can be used to force a specific version of the API endpoint to be used.
         * It is useful for implementing features that rely on an BE changes that affect multiple
         * API endpoints without having to support all changes in the app.
         *
         * **Forced version headers should be removed once the feature is fully implemented**
         */
        const val FORCE_VERSION_HEADER = "X-Force-Version"
    }

    private var okHttpClient: OkHttpClient? = null
    private var currentAuthToken: String? = null

    private val lock = Any() // synchronization lock for the okHttpClient lazy property

    /**
     * Retrieves an instance of [OkHttpClient], ensuring that the same instance is reused unless the authentication token changes.
     *
     * This method is **thread-safe** using `synchronized(lock)`, ensuring that only one instance of `OkHttpClient` is created at a time.
     * If the provided `authToken` differs from the current stored token, a **new OkHttpClient instance** is created.
     * Otherwise, the existing client instance is reused to optimize memory usage and prevent OOM errors.
     *
     * @param authToken The authentication token used for secure API requests. If `null`, a non-authenticated client is used.
     * @param deviceId A unique identifier for the device, added to the request headers.
     * @param versionName The application version name, added to the request headers.
     * @return An instance of [OkHttpClient] configured based on the provided parameters.
     *
     */
    operator fun invoke(
        authToken: String? = null,
        deviceId: String,
        versionName: String,
    ): OkHttpClient = synchronized(lock) {
        if (okHttpClient == null || currentAuthToken != authToken) {
            currentAuthToken = authToken
            okHttpClient = buildOkHttpClient(deviceId, versionName)
        }
        okHttpClient!!
    }

    private fun buildOkHttpClient(
        deviceId: String,
        versionName: String,
    ) = OkHttpClient
        .Builder()
        .cache(networkCache)
        .followRedirects(false)
        .followSslRedirects(false)
        .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
        .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
        .apply {
            if (!currentAuthToken.isNullOrBlank()) {
                addInterceptor(buildAuthenticationInterceptor(currentAuthToken!!))
            }
        }.addNetworkInterceptor(
            ChuckerInterceptor
                .Builder(ctx)
                // Chucker's logging of binary request bodies (e.g., sample uploads) consumes the entire
                // request input stream. For encrypted sample files, this stream cannot be reset,
                // leading to an exhausted or closed stream by the time OkHttp processes it.
                // To prevent interference with sample uploads, we skip the file storage domain entirely.
                .skipDomains("storage.googleapis.com")
                .build(),
        ).addInterceptor(buildDeviceIdInterceptor(deviceId))
        .addInterceptor(buildVersionInterceptor(versionName))
        .addInterceptor(buildGZipInterceptor())
        .apply {
            if (BuildConfig.DEBUG_MODE) {
                addInterceptor(buildSimberLoggingInterceptor())
            }
        }.addInterceptor(buildPersistentLoggerInterceptor())
        .build()

    private fun buildAuthenticationInterceptor(authToken: String) = Interceptor { chain ->
        val newRequest = chain
            .request()
            .newBuilder()
            .addHeader(AUTHORIZATION_HEADER, "Bearer $authToken")
            .build()
        return@Interceptor chain.proceed(newRequest)
    }

    private fun buildDeviceIdInterceptor(deviceId: String) = Interceptor { chain ->
        val newRequest = chain
            .request()
            .newBuilder()
            .addHeader(DEVICE_ID_HEADER, deviceId)
            .build()
        return@Interceptor chain.proceed(newRequest)
    }

    private fun buildSimberLoggingInterceptor(): Interceptor {
        val logger = SimberLogger
        return HttpLoggingInterceptor(logger).apply {
            level = HttpLoggingInterceptor.Level.HEADERS
        }
    }

    private fun buildVersionInterceptor(versionName: String) = Interceptor { chain ->
        val originalRequest = chain.request()
        val version = originalRequest
            .takeIf { BuildConfig.DEBUG }
            ?.header(FORCE_VERSION_HEADER)
            ?: versionName

        val newRequest = originalRequest
            .newBuilder()
            .addHeader(USER_AGENT_HEADER, "SimprintsID/$version")
            .build()
        return@Interceptor chain.proceed(newRequest)
    }

    private fun buildGZipInterceptor() = Interceptor { chain ->
        val originalRequest = chain.request()

        // Only compress requests with explicit gzip header and non-null body
        if (originalRequest.body == null || originalRequest.header("Content-Encoding") != "gzip") {
            return@Interceptor chain.proceed(originalRequest)
        }

        val compressedRequest = originalRequest
            .newBuilder()
            .method(originalRequest.method, originalRequest.body?.let { gzip(it) })
            .build()
        return@Interceptor chain.proceed(compressedRequest)
    }

    private fun buildPersistentLoggerInterceptor() = Interceptor { chain ->
        val originalRequest = chain.request()
        chain.proceed(originalRequest).also { response ->
            persistentLogger.logSync(
                type = LogEntryType.Network,
                timestampMs = response.sentRequestAtMillis,
                title = "${originalRequest.method}: ${originalRequest.url.encodedPath}",
                body =
                    """
                    Host: ${originalRequest.url.host}
                    Request ID: ${originalRequest.header("X-Request-ID")}
                    Response: ${response.code} - ${response.message}
                    """.trimIndent(),
            )
        }
    }

    // First compress the original RequestBody, then wrap it with a new RequestBody to set correct content length
    // https://github.com/square/okhttp/issues/350
    @Throws(IOException::class)
    private fun gzip(requestBody: RequestBody): RequestBody {
        val buffer = Buffer()
        CompressedRequestBody(requestBody).writeTo(buffer)

        return object : RequestBody() {
            override fun contentType() = requestBody.contentType()

            override fun contentLength() = buffer.size

            @Throws(IOException::class)
            override fun writeTo(sink: BufferedSink) {
                sink.write(buffer.snapshot())
            }
        }
    }

    private class CompressedRequestBody(
        private val requestBody: RequestBody,
    ) : RequestBody() {
        override fun contentType(): MediaType? = requestBody.contentType()

        override fun contentLength(): Long = -1 // We don't know the compressed length in advance!

        @Throws(IOException::class)
        override fun writeTo(sink: BufferedSink) {
            GzipSink(sink).apply { deflater.setLevel(6) }.buffer().use { requestBody.writeTo(it) }
        }
    }
}
