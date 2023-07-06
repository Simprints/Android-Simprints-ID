package com.simprints.fingerprint.activities.base

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.simprints.core.livedata.LiveDataEvent
import com.simprints.core.livedata.LiveDataEventObserver
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.livedata.LiveDataEventWithContentObserver
import com.simprints.core.tools.activity.BaseActivity
import com.simprints.fingerprint.tools.extensions.logActivityCreated
import com.simprints.fingerprint.tools.extensions.logActivityDestroyed

/**
 * This class represents any Fingerprint activity within the fingerprint module. It handles common
 * or shared functionality like loading/releasing koin DI for fingerprint and logging activity
 * creation and destruction.
 */
abstract class FingerprintActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logActivityCreated()
    }

    override fun onDestroy() {
        super.onDestroy()
        logActivityDestroyed()
    }

    private fun <T, O : Observer<T>> LiveData<T>.activityObserve(observer: O) =
        observe(this@FingerprintActivity, observer)

    fun <T : Any?> LiveData<T>.activityObserveWith(observer: (T) -> Unit) =
        activityObserve(Observer(observer))

    fun LiveData<LiveDataEvent>.activityObserveEventWith(observer: () -> Unit) =
        activityObserve(LiveDataEventObserver(observer))

    fun <T> LiveData<LiveDataEventWithContent<T>>.activityObserveEventWith(observer: (T) -> Unit) =
        activityObserve(LiveDataEventWithContentObserver(observer))
}
