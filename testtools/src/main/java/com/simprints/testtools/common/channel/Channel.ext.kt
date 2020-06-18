package com.simprints.testtools.common.channel

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.runBlocking

@ExperimentalCoroutinesApi
fun <E> ReceiveChannel<E>.testChannel(): ArrayList<E> {
    val observedValues = arrayListOf<E>()
    runBlocking {
        for(y in this@testChannel) {
            observedValues.add(y)
        }
    }

    return observedValues
}
