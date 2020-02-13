package com.simprints.fingerprintscanner.v2.incoming.root

import com.simprints.fingerprintscanner.v2.domain.root.RootResponse
import com.simprints.fingerprintscanner.v2.incoming.common.MessageInputStream
import com.simprints.fingerprintscanner.v2.tools.reactive.filterCast
import com.simprints.fingerprintscanner.v2.tools.reactive.subscribeOnIoAndPublish
import com.simprints.fingerprintscanner.v2.tools.reactive.toFlowable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import java.io.InputStream

class RootMessageInputStream(private val rootResponseAccumulator: RootResponseAccumulator) : MessageInputStream {

    var rootResponseStream: Flowable<RootResponse>? = null

    private var rootResponseStreamDisposable: Disposable? = null

    override fun connect(inputStream: InputStream) {
        rootResponseStream = transformToRootResponseStream(inputStream)
            .subscribeOnIoAndPublish()
            .also {
                rootResponseStreamDisposable = it.connect()
            }
    }

    private fun transformToRootResponseStream(inputStream: InputStream) =
        inputStream
            .toFlowable()
            .toRootMessageStream(rootResponseAccumulator)

    override fun disconnect() {
        rootResponseStreamDisposable?.dispose()
    }

    inline fun <reified R : RootResponse> receiveResponse(): Single<R> =
        Single.defer {
            rootResponseStream
                ?.filterCast<R>()
                ?.firstOrError()
                ?: Single.error(IllegalStateException("Trying to receive response before connecting stream"))
        }
}
