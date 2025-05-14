package com.simprints.testtools.common.coroutines

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.runBlocking

fun <T> createTestChannel(vararg lists: List<T>): ReceiveChannel<List<T>> {
    val channel = Channel<List<T>>(lists.size)
    runBlocking {
        for (list in lists) {
            channel.send(list)
        }
        channel.close()
    }
    return channel
}
