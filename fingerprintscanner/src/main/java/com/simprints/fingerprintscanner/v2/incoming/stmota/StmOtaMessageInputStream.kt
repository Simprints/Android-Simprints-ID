package com.simprints.fingerprintscanner.v2.incoming.stmota

import com.simprints.fingerprintscanner.v2.domain.stmota.StmOtaResponse
import com.simprints.fingerprintscanner.v2.incoming.common.MessageInputStream
import com.simprints.fingerprintscanner.v2.tools.reactive.filterCast
import com.simprints.fingerprintscanner.v2.tools.reactive.subscribeOnIoAndPublish
import com.simprints.fingerprintscanner.v2.tools.reactive.toFlowable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import java.io.InputStream

class StmOtaMessageInputStream(private val stmOtaResponseParser: StmOtaResponseParser) : MessageInputStream {

    var stmOtaResponseStream: Flowable<StmOtaResponse>? = null

    private var stmOtaResponseStreamDisposable: Disposable? = null

    override fun connect(inputStream: InputStream) {
        stmOtaResponseStream = transformToStmOtaResponseStream(inputStream)
            .subscribeOnIoAndPublish()
            .also {
                stmOtaResponseStreamDisposable = it.connect()
            }
    }

    private fun transformToStmOtaResponseStream(inputStream: InputStream) =
        inputStream
            .toFlowable()
            .map { stmOtaResponseParser.parse(it) }

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
