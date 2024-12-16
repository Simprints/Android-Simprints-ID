package com.simprints.testtools.common.livedata

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

class TestObserver<T> : Observer<T?> {
    val observedValues = mutableListOf<T?>()

    override fun onChanged(value: T?) {
        observedValues.add(value)
    }
}

@Deprecated("Use getOrAwaitValue")
fun <T> LiveData<T>.testObserver() = TestObserver<T>().also {
    observeForever(it)
}

@Deprecated("Use getOrAwaitValue")
fun <T> MutableLiveData<T>.testObserver() = TestObserver<T>().also {
    observeForever(it)
}

/**
 * Gets the value of a [LiveData] or waits for it to have one, with a timeout.
 *
 * Use this extension from host-side (JVM) tests. It's recommended to use it alongside
 * `InstantTaskExecutorRule` or a similar mechanism to execute tasks synchronously.
 */
fun <T> LiveData<T>.getOrAwaitValue(
    time: Long = 5,
    timeUnit: TimeUnit = TimeUnit.SECONDS,
    afterObserve: () -> Unit = {},
): T {
    var data: T? = null
    val latch = CountDownLatch(1)
    val observer = object : Observer<T> {
        override fun onChanged(o: T) {
            data = o
            latch.countDown()
            this@getOrAwaitValue.removeObserver(this)
        }
    }
    this.observeForever(observer)

    afterObserve.invoke()

    // Don't wait indefinitely if the LiveData is not set.
    if (!latch.await(time, timeUnit)) {
        this.removeObserver(observer)
        throw TimeoutException("LiveData value was never set.")
    }

    @Suppress("UNCHECKED_CAST")
    return data as T
}

fun <T> LiveData<T>.getOrAwaitValues(
    number: Int,
    time: Long = 5,
    timeUnit: TimeUnit = TimeUnit.SECONDS,
    afterObserve: () -> Unit = {},
): List<T> {
    val data: MutableList<T> = mutableListOf()
    val latch = CountDownLatch(number)
    val observer = object : Observer<T> {
        override fun onChanged(o: T) {
            data.add(o)
            latch.countDown()
            if (data.size >= number) {
                this@getOrAwaitValues.removeObserver(this)
            }
        }
    }
    this.observeForever(observer)

    afterObserve.invoke()

    // Don't wait indefinitely if the LiveData is not set.
    if (!latch.await(time, timeUnit)) {
        this.removeObserver(observer)
        throw TimeoutException("LiveData value was never set $number times.")
    }

    return data
}
