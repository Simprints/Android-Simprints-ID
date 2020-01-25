package com.simprints.fingerprintscanner.v2.incoming.stmota

import com.simprints.fingerprintscanner.v2.domain.stmota.StmOtaResponse
import com.simprints.fingerprintscanner.v2.incoming.IncomingConnectable
import com.simprints.fingerprintscanner.v2.tools.reactive.filterCast
import com.simprints.fingerprintscanner.v2.tools.reactive.toFlowable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.io.InputStream

class StmOtaMessageInputStream(private val stmOtaResponseParser: StmOtaResponseParser) : IncomingConnectable {

    var stmOtaResponseStream: Flowable<StmOtaResponse>? = null

    private lateinit var stmOtaResponseStreamDisposable: Disposable

    override fun connect(inputStream: InputStream) {
        stmOtaResponseStream = transformToRootResponseStream(inputStream)
            .subscribeAndPublish()
            .also {
                stmOtaResponseStreamDisposable = it.connect()
            }
    }

    private fun transformToRootResponseStream(inputStream: InputStream) =
        inputStream
            .toFlowable()
            .map { stmOtaResponseParser.parse(it) }

    override fun disconnect() {
        stmOtaResponseStreamDisposable.dispose()
    }

    private fun Flowable<StmOtaResponse>.subscribeAndPublish() =
        this.subscribeOn(Schedulers.io()).publish()

    inline fun <reified R : StmOtaResponse> receiveResponse(): Single<R> =
        Single.defer {
            stmOtaResponseStream
                ?.filterCast<R>()
                ?.firstOrError()
                ?: Single.error(IllegalStateException("Trying to receive response before connecting stream"))
        }
}
