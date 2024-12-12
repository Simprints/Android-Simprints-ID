package com.simprints.fingerprint.infra.scanner.v2.incoming.stmota

import com.simprints.core.DispatcherIO
import com.simprints.fingerprint.infra.scanner.v2.domain.stmota.StmOtaResponse
import com.simprints.fingerprint.infra.scanner.v2.incoming.common.MessageInputStream
import com.simprints.fingerprint.infra.scanner.v2.tools.reactive.filterCast
import com.simprints.fingerprint.infra.scanner.v2.tools.reactive.subscribeOnIoAndPublish
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

/**
 * Takes an InputStream and transforms it into a Flowable<StmOtaResponse> for use while the Vero is
 * in STM OTA Mode.
 */
class StmOtaMessageInputStream @Inject constructor(
    private val stmOtaResponseParser: StmOtaResponseParser,
    @DispatcherIO private val ioDispatcher: CoroutineDispatcher,
) : MessageInputStream {
    var stmOtaResponseStream: Flowable<StmOtaResponse>? = null

    private var stmOtaResponseStreamDisposable: Disposable? = null

    override fun connect(flowableInputStream: Flowable<ByteArray>) {
        stmOtaResponseStream = transformToStmOtaResponseStream(flowableInputStream)
            .subscribeOnIoAndPublish(ioDispatcher)
            .also {
                stmOtaResponseStreamDisposable = it.connect()
            }
    }

    private fun transformToStmOtaResponseStream(flowableInputStream: Flowable<ByteArray>) =
        flowableInputStream.map { stmOtaResponseParser.parse(it) }

    override fun disconnect() {
        stmOtaResponseStreamDisposable?.dispose()
    }

    inline fun <reified R : StmOtaResponse> receiveResponse(): Single<R> = Single.defer {
        stmOtaResponseStream
            ?.filterCast<R>()
            ?.firstOrError()
            ?: Single.error(IllegalStateException("Trying to receive response before connecting stream"))
    }
}
