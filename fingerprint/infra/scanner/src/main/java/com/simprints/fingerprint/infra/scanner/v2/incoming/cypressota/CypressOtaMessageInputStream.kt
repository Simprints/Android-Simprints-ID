package com.simprints.fingerprint.infra.scanner.v2.incoming.cypressota

import com.simprints.core.DispatcherIO
import com.simprints.fingerprint.infra.scanner.v2.domain.cypressota.CypressOtaResponse
import com.simprints.fingerprint.infra.scanner.v2.incoming.common.MessageInputStream
import com.simprints.fingerprint.infra.scanner.v2.tools.reactive.filterCast
import com.simprints.fingerprint.infra.scanner.v2.tools.reactive.subscribeOnIoAndPublish
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

/**
 * Takes an InputStream and transforms it into a Flowable<CypressOtaResponse> for use while the Vero
 * is in Cypress OTA Mode.
 */
class CypressOtaMessageInputStream @Inject constructor(
    private val cypressOtaResponseParser: CypressOtaResponseParser,
    @DispatcherIO private val ioDispatcher: CoroutineDispatcher,
    ) : MessageInputStream {

    var cypressOtaResponseStream: Flowable<CypressOtaResponse>? = null

    private var cypressOtaResponseStreamDisposable: Disposable? = null

    override fun connect(flowableInputStream: Flowable<ByteArray>) {
        cypressOtaResponseStream = transformToCypressOtaResponseStream(flowableInputStream)
            .subscribeOnIoAndPublish(ioDispatcher)
            .also {
                cypressOtaResponseStreamDisposable = it.connect()
            }
    }

    private fun transformToCypressOtaResponseStream(flowableInputStream: Flowable<ByteArray>) =
        flowableInputStream.map { cypressOtaResponseParser.parse(it) }

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
