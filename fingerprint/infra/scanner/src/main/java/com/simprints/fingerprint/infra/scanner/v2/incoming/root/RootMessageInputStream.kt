package com.simprints.fingerprint.infra.scanner.v2.incoming.root

import com.simprints.core.DispatcherIO
import com.simprints.fingerprint.infra.scanner.v2.domain.root.RootResponse
import com.simprints.fingerprint.infra.scanner.v2.incoming.common.MessageInputStream
import com.simprints.fingerprint.infra.scanner.v2.tools.reactive.filterCast
import com.simprints.fingerprint.infra.scanner.v2.tools.reactive.subscribeOnIoAndPublish
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

/**
 * Takes an InputStream and transforms it into a Flowable<RootResponse> for use while the Vero is in
 * Root Mode.
 */
class RootMessageInputStream @Inject constructor(
    private val rootResponseAccumulator: RootResponseAccumulator,
    @DispatcherIO private val ioDispatcher: CoroutineDispatcher,
) : MessageInputStream {

    var rootResponseStream: Flowable<RootResponse>? = null

    private var rootResponseStreamDisposable: Disposable? = null

    override fun connect(flowableInputStream: Flowable<ByteArray>) {
        rootResponseStream = transformToRootResponseStream(flowableInputStream)
            .subscribeOnIoAndPublish(ioDispatcher)
            .also {
                rootResponseStreamDisposable = it.connect()
            }
    }

    private fun transformToRootResponseStream(flowableInputStream: Flowable<ByteArray>) =
        flowableInputStream.toRootMessageStream(rootResponseAccumulator)

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
