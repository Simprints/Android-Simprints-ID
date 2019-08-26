package com.simprints.testtools.common.blocking

import java.util.concurrent.LinkedBlockingQueue

class BlockingFlag {

    private val blockingQueue = LinkedBlockingQueue<Unit>()

    fun await(): Unit = blockingQueue.take()
    fun finish(): Unit = blockingQueue.put(Unit)
}
