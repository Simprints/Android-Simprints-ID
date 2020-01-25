package com.simprints.fingerprintscanner.v2.incoming.root

import com.simprints.fingerprintscanner.v2.domain.root.RootResponse
import com.simprints.fingerprintscanner.v2.incoming.IncomingConnectable
import com.simprints.fingerprintscanner.v2.tools.reactive.filterCast
import com.simprints.fingerprintscanner.v2.tools.reactive.toFlowable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.io.InputStream

class RootMessageInputStream(private val rootResponseAccumulator: RootResponseAccumulator) : IncomingConnectable {

    var rootResponseStream: Flowable<RootResponse>? = null

    private lateinit var rootResponseStreamDisposable: Disposable

    override fun connect(inputStream: InputStream) {
        rootResponseStream = transformToRootResponseStream(inputStream)
            .subscribeAndPublish()
            .also {
                rootResponseStreamDisposable = it.connect()
            }
    }

    private fun transformToRootResponseStream(inputStream: InputStream) =
        inputStream
            .toFlowable()
            .toRootMessageStream(rootResponseAccumulator)

    override fun disconnect() {
        rootResponseStreamDisposable.dispose()
    }

    private fun Flowable<RootResponse>.subscribeAndPublish() =
        this.subscribeOn(Schedulers.io()).publish()

    inline fun <reified R : RootResponse> receiveResponse(): Single<R> =
        Single.defer {
            rootResponseStream
                ?.filterCast<R>()
                ?.firstOrError()
                ?: Single.error(IllegalStateException("Trying to receive response before connecting stream"))
        }
}
