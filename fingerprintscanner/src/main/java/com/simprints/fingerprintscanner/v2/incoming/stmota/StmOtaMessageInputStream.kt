package com.simprints.fingerprintscanner.v2.incoming.stmota

import com.simprints.fingerprintscanner.v2.domain.stmota.StmOtaResponse
import com.simprints.fingerprintscanner.v2.incoming.common.MessageInputStream
import com.simprints.fingerprintscanner.v2.tools.reactive.filterCast
import com.simprints.fingerprintscanner.v2.tools.reactive.subscribeOnIoAndPublish
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.disposables.Disposable

/**
 * Takes an InputStream and transforms it into a Flowable<StmOtaResponse> for use while the Vero is
 * in STM OTA Mode.
 */
class StmOtaMessageInputStream(private val stmOtaResponseParser: StmOtaResponseParser) : MessageInputStream {

    var stmOtaResponseStream: Flowable<StmOtaResponse>? = null

    private var stmOtaResponseStreamDisposable: Disposable? = null

    override fun connect(flowable: Flowable<ByteArray>) {
        stmOtaResponseStream = transformToStmOtaResponseStream(flowable)
            .subscribeOnIoAndPublish()
            .also {
                stmOtaResponseStreamDisposable = it.connect()
            }
    }

    private fun transformToStmOtaResponseStream(flowable: Flowable<ByteArray>) =
        flowable.map { stmOtaResponseParser.parse(it) }

    override fun disconnect() {
        stmOtaResponseStreamDisposable?.dispose()
    }

    inline fun <reified R : StmOtaResponse> receiveResponse(): Single<R> =
        Single.defer {
            stmOtaResponseStream
                ?.filterCast<R>()
                ?.firstOrError()
                ?: Single.error(IllegalStateException("Trying to receive response before connecting stream"))
        }
}
