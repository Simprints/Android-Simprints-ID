package com.simprints.fingerprintscanner.v2.incoming.cypressota

import com.simprints.fingerprintscanner.v2.domain.cypressota.CypressOtaResponse
import com.simprints.fingerprintscanner.v2.incoming.common.MessageInputStream
import com.simprints.fingerprintscanner.v2.tools.reactive.filterCast
import com.simprints.fingerprintscanner.v2.tools.reactive.subscribeOnIoAndPublish
import com.simprints.fingerprintscanner.v2.tools.reactive.toFlowable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import java.io.InputStream

class CypressOtaMessageInputStream(private val cypressOtaResponseParser: CypressOtaResponseParser) : MessageInputStream {

    var cypressOtaResponseStream: Flowable<CypressOtaResponse>? = null

    private var cypressOtaResponseStreamDisposable: Disposable? = null

    override fun connect(inputStream: InputStream) {
        cypressOtaResponseStream = transformToCypressOtaResponseStream(inputStream)
            .subscribeOnIoAndPublish()
            .also {
                cypressOtaResponseStreamDisposable = it.connect()
            }
    }

    private fun transformToCypressOtaResponseStream(inputStream: InputStream) =
        inputStream
            .toFlowable()
            .map { cypressOtaResponseParser.parse(it) }

    override fun disconnect() {
        cypressOtaResponseStreamDisposable?.dispose()
    }

    inline fun <reified R : CypressOtaResponse> receiveResponse(): Single<R> =
        Single.defer {
            cypressOtaResponseStream
                ?.filterCast<R>()
                ?.firstOrError()
                ?: Single.error(IllegalStateException("Trying to receive response before connecting stream"))
        }
}
