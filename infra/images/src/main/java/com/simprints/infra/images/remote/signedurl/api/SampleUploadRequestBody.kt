package com.simprints.infra.images.remote.signedurl.api

import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import kotlinx.serialization.Serializable
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.BufferedSink
import okio.source
import java.io.FileInputStream
import java.io.IOException

@ExcludedFromGeneratedTestCoverageReports("OkHttp specific code that is not possible to test in isolation")
internal class SampleUploadRequestBody(
    private val inputStream: FileInputStream,
    private val size: Long,
) : RequestBody() {
    override fun contentType(): MediaType? = "application/octet-stream".toMediaTypeOrNull()

    override fun contentLength(): Long = size

    @Throws(IOException::class)
    override fun writeTo(sink: BufferedSink) {
        inputStream.source().use { source -> sink.writeAll(source) }
    }
}
