package com.simprints.id.tools.extensions

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consume
import kotlinx.coroutines.channels.receiveOrNull
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlin.coroutines.coroutineContext

@InternalCoroutinesApi
@ExperimentalCoroutinesApi
suspend fun <T> Flow<T>.bufferedChunks(maxChunkSize: Int): Flow<List<T>> = channelFlow<List<T>> {
    require(maxChunkSize >= 1) {
        "Max chunk size should be greater than 0 but was $maxChunkSize"
    }
    val buffer = ArrayList<T>(maxChunkSize)

    try {
        collect {
            buffer += it
            if (buffer.size == maxChunkSize) {
                send(buffer.toList())
                buffer.clear()
            }
        }
    } finally {
        send(buffer.toList())
        buffer.clear()
    }

//    this@bufferedChunks.onCompletion {
//        send(buffer.toList())
//        buffer.clear()
//    }

}.buffer(1)

suspend fun <E : Any> Channel<E>.consumeEachBlock(maxBlockSize: Int, consumer: (List<E>) -> Unit) {
    consume {
        val buffer = ArrayList<E>(maxBlockSize)
        while (coroutineContext[Job]?.isActive != false) {
            buffer += receiveOrNull() ?: return
            while (buffer.size < maxBlockSize) {
                val element = poll() ?: break
                buffer += element
            }
            consumer(buffer)
            buffer.clear()
        }
    }
}
