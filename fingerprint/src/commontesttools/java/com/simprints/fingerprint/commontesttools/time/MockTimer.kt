package com.simprints.fingerprint.commontesttools.time

import java.util.*

class MockTimer : Timer() {

    private var taskQueue = LinkedList<TimerTask>()

    override fun purge(): Int {
        val numItems = taskQueue.size
        taskQueue = LinkedList<TimerTask>()
        return numItems
    }

    override fun schedule(task: TimerTask?, delay: Long) {
        task?.let { taskQueue.add(it) }
    }

    override fun schedule(task: TimerTask?, time: Date?) {
        task?.let { taskQueue.add(it) }
    }

    override fun schedule(task: TimerTask?, delay: Long, period: Long) {
        task?.let { taskQueue.add(it) }
    }

    override fun schedule(task: TimerTask?, firstTime: Date?, period: Long) {
        task?.let { taskQueue.add(it) }
    }

    override fun scheduleAtFixedRate(task: TimerTask?, delay: Long, period: Long) {
        task?.let { taskQueue.add(it) }
    }

    override fun scheduleAtFixedRate(task: TimerTask?, firstTime: Date?, period: Long) {
        task?.let { taskQueue.add(it) }
    }

    override fun cancel() {
        taskQueue = LinkedList<TimerTask>()
    }

    fun executeNextTask() {
        taskQueue.remove().run()
    }

    fun executeAllTasks() {
        while (taskQueue.isNotEmpty()) {
            executeNextTask()
        }
    }
}
