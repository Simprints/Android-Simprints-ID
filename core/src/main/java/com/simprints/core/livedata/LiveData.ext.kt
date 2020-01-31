package com.simprints.core.livedata

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.simprints.core.tools.extentions.resumeSafely
import kotlinx.coroutines.suspendCancellableCoroutine

suspend fun <T> LiveData<T>.waitForAValue(lifecycleOwner: LifecycleOwner) = suspendCancellableCoroutine<T> { cont ->
    this.observe(lifecycleOwner, Observer {
        cont.resumeSafely(it)
    })
}
